(ns wcig.db
  (:require
    [monger.conversion :refer [from-db-object]]
    [monger.collection :as mc]
    [monger.query :as q]
    [monger.operators :refer :all]
    [monger.joda-time]
    [jota.core :as log]
    [wcig.dbbase :refer :all]
    [wcig.util :refer :all]
    [wcig.data :as data])
  (:import (org.joda.time DateTime)
           ))


(def QPX-CALLS "qpx-calls")
(def USER-EVENTS "uevents")
(def AIRPORTS "airports")

; VIEWS

(def TRANSAVIA-ROUTES "view-transavia-routes")
(def FLIGHTS-ON-DATE "view-flights-on-day")
(def LATEST-ITINERARY "view-latest-itinerary")
(def SLICES "view-slices")



(defn timestamp
  ([col ts] (assoc col :timestamp ts))
  ([col] (timestamp col (now))))




(defn save-transavia-routes-from-origin [routes]
  "Writes transavia routes per origin"
  (mc/upsert @db TRANSAVIA-ROUTES
             {:origin (:origin routes)} routes))


(defn get-transavia-destnations [origins]
  "Retreives all destinations (as list of codes) with an optional list of origins as a filter"
  (let [condition (if origins {:origin {$in origins}} {})
        routes (mc/find-maps @db TRANSAVIA-ROUTES condition)]
    (set (mapcat :destinations routes))))



;-- airports
(defn save-airport [a]
  (mc/save @db AIRPORTS (timestamp a)))
(defn save-airports [as]
  (doall (map #(mc/save @db AIRPORTS (timestamp %)) as)))


(defn find-airport [code] (mc/find-one-as-map @db AIRPORTS {:code code}))
(defn find-airport-city [code] (mc/find-one-as-map @db AIRPORTS {:city-code code}))
(defn find-airports-in [code] (mc/find-maps @db AIRPORTS {:city-code code}))
(defn find-airports [code]
  "Returns an airport or a list of airports in city"
  {:pre [(string? code)]}
  (if (find-airport-city code)
    (find-airports-in code)
    (find-airport code)))


; -- transavia



(defn airports-time-dif [code1 code2]
  "Time difference betweeen two airports"
  (let
    [
     tz1 (:tz (find-airport code1))
     tz2 (:tz (find-airport code2))
     ]
    (cond
      (nil? (:rawOffset tz1)) (do (log/error "Error tz " code1) 0)
      (nil? (:rawOffset tz2)) (do (log/error "Error tz " code2) 0)
      :default
      (- (:rawOffset tz2) (:rawOffset tz1)))
    ))


(defn find-itineraries [origin ^String out-date ^String dest in-date]
  (mc/find-maps @db LATEST-ITINERARY
                {
                 :origin      origin
                 :destination dest
                 :outDate     out-date
                 :inDate      in-date
                 }))




(defn find-qpx-itineraries
  ([origin ^String out-date-s ^String in-date-s params]
   (log/info "find-qpx-itineraries:[" origin "] " out-date-s "/" in-date-s " params: " params)
   (let [
         dates (make-search-dates out-date-s in-date-s (:around params))
         dests (if-let [dests (:dests params)] dests data/all-airports)

         destinations (map find-airports dests)

         destinations (flatten destinations)
         destinations (map :code destinations)
         ]
     (mapcat (fn [[o i]]
               (mapcat (fn [dest] (find-itineraries origin o dest i)) destinations)
               )
             dates)
     ))
  ([origin ^String out-date-s ^String in-date-s] (find-qpx-itineraries origin out-date-s in-date-s {})
    ))

(defn save-qpx-call [args]
  (mc/save @db QPX-CALLS (timestamp {:args args})))

(defn find-qpx-call [args]
  (first
    (q/with-collection
      @db QPX-CALLS
      (q/find {:args args})
      (q/sort (array-map :timestamp -1))
      (q/limit 1)
      )))

(defn find-qpx-calls-today []
  (let [dt (DateTime.)]
    (count
      (mc/find-maps @db QPX-CALLS
                    {
                     :timestamp {$lt (.getMillis (to-end-day dt))
                                 $gt (.getMillis (to-beginning-day dt))}
                     })))
  )

(defn save-event [evt]
  (mc/save @db USER-EVENTS evt))




(defn get-user-events []
  (let [events (q/with-collection
                 @db USER-EVENTS
                 (q/find {$and [{:action {$ne :access}} {:action {$ne :itineraries}}]})
                 (q/sort (array-map :ts -1))
                 (q/limit 1000))]
    (map (fn [e] (-> e (dissoc :_id))) events)
    ))


(defn save-flights-on-date [f]
  (mc/upsert @db FLIGHTS-ON-DATE
             {
              :origin        (:origin f)
              :destination   (:destination f)
              :departureDate (:departureDate f)
              }
             f
             ))

(defn find-flights-from-on-dates [origin dates dests group]
  (log/debug "find flights on " [origin dates dests group])
  (q/with-collection
    @db FLIGHTS-ON-DATE
    (q/find {:origin        origin
             :destination   {$in dests}
             :departureDate {$in dates}
             :group         group})
    (q/sort {:timestamp -1})
    ))

(defn find-itineraries-from-on-dates [origin date-combinations group]
  (log/debug "find itineraries on " [origin date-combinations group])
  (let [its (for [dc date-combinations]
              (do
                (q/with-collection
                  @db LATEST-ITINERARY
                  (q/find {:origin  origin
                           :outDate (first dc)
                           :inDate  (second dc)
                           :group   group})
                  (q/sort {:timestamp -1})
                  )))]
    (mapcat identity its)))

(defn find-slices-from-to-on-dates [origin out-date destination in-date group]
  (mc/find-one-as-map
    @db SLICES
    {:origin      origin
     :outDate     out-date
     :inDate      in-date
     :destination destination}))

(defn find-flights-to-on-dates [origin dates dests group]
  (log/debug "find flights on " [origin dates dests group])
  (q/with-collection
    @db FLIGHTS-ON-DATE
    (q/find {:origin        {$in dests}
             :destination   origin
             :departureDate {$in dates}
             :group         group})
    (q/sort {:timestamp -1})
    ))

(defn upsert-itineraries [rec]
  (log/debug "about to upsert " rec)
  (mc/upsert @db
             LATEST-ITINERARY
             {
              :origin      (:origin rec)
              :destination (:destination rec)
              :inDate      (:inDate rec)
              :outDate     (:outDate rec)
              }
             rec
             )
  )

(defn upsert-slices [s]
  (mc/upsert @db SLICES
             {
              :origin      (:origin s)
              :destination (:destination s)
              :inDate      (:inDate s)
              :outDate     (:outDate s)
              }
             s
             ))

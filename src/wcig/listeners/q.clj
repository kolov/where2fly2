(ns wcig.listeners.q
  (:require

    [wcig.util :refer :all]
    [slingshot.slingshot :refer [throw+]]
    [monger.operators :refer :all]
    [wcig.dbbase :refer :all]
    [wcig.db :as db]
    )
  (:import (org.joda.time Duration)
           (java.util UUID)))



(defn- parse-price [s] (.intValue (bigdec (.substring s 3))))

(defn drop-nil-values [m] (into {} (filter second m)))

(defn make-tripOption [tripOption]
  (let [
        pricing (first (:pricing tripOption))               ;; why array?
        slices (:slice tripOption)
        parse-segment (fn [segment] (drop-nil-values {
                                                      :aircraft           (-> segment :leg first :aircraft)
                                                      :departureTime      (-> segment :leg first :departureTime to-local-datetime-in-utc)
                                                      :arrivalTime        (-> segment :leg last :arrivalTime to-local-datetime-in-utc)
                                                      :duration           (-> segment :duration)
                                                      :connectionDuration (-> segment :connectionDuration)
                                                      :carrier            (-> segment :flight :carrier)
                                                      :number             (-> segment :flight :number)
                                                      :origin             (-> segment :leg first :origin)
                                                      :destination        (-> segment :leg first :destination)
                                                      }))
        itinerary
        {:price                (parse-price (:saleTotal pricing))
         :currency             (.substring (:saleTotal pricing) 0 3)

         :slice                (map (fn [slice] (map (fn [segment] (parse-segment segment)) (:segment slice))) slices)

         :outDepartureDateTime (-> slices first :segment first :leg first :departureTime to-local-datetime-in-utc)
         :outArrivalDateTime   (-> slices first :segment last :leg last :arrivalTime to-local-datetime-in-utc)
         :inDepartureDateTime  (-> slices last :segment first :leg first :departureTime to-local-datetime-in-utc)
         :inArrivalDateTime    (-> slices last :segment last :leg last :arrivalTime to-local-datetime-in-utc)

         :outDuration          (.getStandardMinutes
                                 (Duration.
                                   (-> slices first :segment first :leg first :departureTime parse-iso-datetime)
                                   (-> slices first :segment last :leg last :arrivalTime parse-iso-datetime)))
         :inDuration           (.getStandardMinutes
                                 (Duration.
                                   (-> slices last :segment first :leg first :departureTime parse-iso-datetime)
                                   (-> slices last :segment last :leg last :arrivalTime parse-iso-datetime)))
         }
        ]
    (drop-nil-values itinerary)))

(defn make-qpx-itinerary [rec]
  {
   :origin      (:origin rec)
   :destination (:destination rec)
   :outDate     (:outDate rec)
   :inDate      (:inDate rec)
   :group       :qpx
   :itineraries (map make-tripOption (:tripOptions rec))
   })


(defn split-itineraries [rec]
  [
   (update-in rec [:itineraries] (fn [its] (map #(dissoc % :slice) its)))

   {
    :slices      (map :slice (:itineraries rec))
    :origin      (:origin rec)
    :destination (:destination rec)
    :inDate      (:inDate rec)
    :outDate     (:outDate rec)
    }
   ])

(defn qpx-trip-options-listener [evt]
  {:pre [(= (:type evt) :fetched-qpx-trip-options)]}
  (println "qpx-trip-options-listener called")
  (let [
        [itineraries-record slices-record] (split-itineraries (make-qpx-itinerary evt))
        ]
    (db/upsert-itineraries itineraries-record)
    (db/upsert-slices slices-record))
  )


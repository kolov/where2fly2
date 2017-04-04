(ns wcig.core
  (:require
    [wcig.db :as db]

    [taoensso.timbre :as log]
    [slingshot.slingshot :refer [throw+]]
    [wcig.util :refer :all]
    [wcig.connectors.google :as google]
    [wcig.connectors.transavia :as transavia]
    [wcig.connectors.qpx :as qpx]
    [monger.operators :refer :all]
    [pulse.core :as p]
    )
  (:import (org.joda.time DateTime)))


(defn log-error [& args] (log/error (apply str args)))


;- TRANSAVIA
(def transavia-origins ["AMS" "RTM" "EIN" "GRQ"])
(def MONTH-AHEAD 6)

(defn update-transavia-routes []
  "GLOBAL: Fetches all transavia routes and publishes them as one event"
  (log/info "update-transavia-routes called")
  (p/publish {:type :fetched-transavia-routes :routes (transavia/get-routes)}))

;-- itineraries


(defn fetch-transavia-itineraries-one-way [origin dest year month]
  (let [response (transavia/get-flight-offers-month origin dest year month)]
    (p/publish {:type :fetched-transavia-flight-offers :response response})))


(defn fetch-transavia-itineraries-both-ways [origin dest year month]
  (log/debug "Fetching transavia " origin dest year month)
  (fetch-transavia-itineraries-one-way origin dest year month)
  (fetch-transavia-itineraries-one-way dest origin year month)
  )

(defn update-transavia-itineraries
  ([origin dests year month]
   (log/info "update-transavia-itineraries" origin year month dests)
   (doseq [dest dests]
     (fetch-transavia-itineraries-both-ways origin dest year month)))
  ([origin year month]
   "Method to call to update all transavia itineraries regularly"
   (log/info "update-transavia-itineraries" origin year month)
   (update-transavia-itineraries origin (db/get-transavia-destnations [origin]) year month)))



(defn update-transavia-flightsinfo []
  "Updates actual flights information for the coming 3 months"
  (log/info "update-transavia-flightsinfo")
  (let [now (DateTime.)
        months (for [n (range MONTH-AHEAD)] (.plusMonths now n))]
    (doseq [m months o transavia-origins]
      (update-transavia-itineraries o (.get (.year m)) (.get (.monthOfYear m))))

    ))
; -- QPX




(defn get-qpx-itineraries [^String origin ^String o-date ^String dest ^String i-date]
  (let [response (qpx/find-sync origin o-date dest i-date)
        tripOptions (get-in response [:trips :tripOption])
        ]
    (p/publish {:type        :fetched-qpx-trip-options
                :tripOptions tripOptions
                :origin      origin
                :outDate     o-date
                :destination dest
                :inDate      i-date
                })))


;--- GET

(defn google-update-airport [code]
  "Fetches airport location by creaatively adapting airport code. Unreliable."
  (if-let [airport (google/fetch-airport code)]
    (do (db/save-airport airport) airport)
    (log-error "Could not find airport " code)))


;---- itineraries





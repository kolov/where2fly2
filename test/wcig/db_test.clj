(ns wcig.db-test
    (:require
      [clojure.test :refer :all]
      [clojure.pprint :refer :all]
      [wcig.db :refer :all]
      [wcig.dbbase :refer :all]
      [wcig.util :refer :all]
      [midje.sweet :refer :all]
      [monger.core :as mg]
      [monger.collection :as mc]
      [wcig.db :as db]))


(def itinerary {
                :outDeparture       (to-airport-time "2015-07-24T10:00:00.000Z")
                :outArrival         (to-airport-time "2015-07-24T13:00:00.000Z")
                :inDeparture        (to-airport-time "2015-07-25T17:00:00.000Z")
                :inArrival          (to-airport-time "2015-07-25T20:00:00.000Z")
                :originAirport      "AMS", :outDuration 375, :currency "EUR",
                :destinationAirport "ACE",
                :priceOnePassenger  533,
                :slice              [
                                     [{:meal                "Food and Beverages for Purchase",
                                       :aircraft            "320",
                                       :number              "8301",
                                       :departureTime       (to-airport-time "2015-07-24T10:00:00.000Z")
                                       :arrivalTime         (to-airport-time "2015-07-24T11:00:00.000Z")
                                       :operatingDisclosure nil,
                                       :duration            135,
                                       :carrier             "VY",
                                       :connectionDuration  55,
                                       :origin              "AMS",
                                       :destination         "BCN"}
                                      {:meal                "Food and Beverages for Purchase",
                                       :aircraft            "320",
                                       :number              "2472",
                                       :departureTime       (to-airport-time "2015-07-24T12:00:00.000Z")
                                       :arrivalTime         (to-airport-time "2015-07-24T13:00:00.000Z")
                                       :operatingDisclosure nil, :duration 185,
                                       :carrier             "VY",
                                       :connectionDuration  nil, :origin "BCN",
                                       :destination         "ACE"}]
                                     [{:meal                "Food and Beverages for Purchase",
                                       :aircraft            "321",
                                       :number              "2473",
                                       :departureTime       (to-airport-time "2015-07-25T17:00:00.000Z")
                                       :arrivalTime         (to-airport-time "2015-07-25T18:00:00.000Z")
                                       :operatingDisclosure nil, :duration 175, :carrier "VY",
                                       :connectionDuration  675, :origin "ACE", :destination "BCN"}
                                      {:meal                "Food and Beverages for Purchase",
                                       :aircraft            "320",
                                       :number              "8300",
                                       :departureTime       (to-airport-time "2015-07-25T19:00:00.000Z")
                                       :arrivalTime         (to-airport-time "2015-07-25T20:00:00.000Z")
                                       :operatingDisclosure nil, :duration 130, :carrier "VY",
                                       :connectionDuration  nil, :origin "BCN", :destination "AMS"}]],
                :inDuration         980,
                :outArrivalDateTime (to-airport-time "2015-07-24T22:00:00.000Z")
                :timestamp          1437080275978,
                })

(def transavia1 {
                 :flightSegment           {
                                           :originAirport      "AMS",
                                           :departureTime      (parse-iso-datetime "2015-10-05T20:30:00.000Z"),
                                           :arrivalTime        (parse-iso-datetime "2015-10-06T02:00:00.000Z"),
                                           :carrier            "HV",
                                           :flightNumber       626,
                                           :destinationAirport "SPC",
                                           },
                 :airItineraryPricingInfo {
                                           :currencyCode            "EUR",
                                           :baseFare                207.01,
                                           :taxSurcharge            8.99,
                                           :totalPriceOnePassenger  216,
                                           :totalPriceAllPassengers 216,
                                           :bookingFee              0
                                           }
                 :timestamp               1
                 })

(def transavia2 {
                 :flightSegment           {
                                           :originAirport      "SPC",
                                           :departureTime      (parse-iso-datetime "2015-10-12T20:30:00.000Z"),
                                           :arrivalTime        (parse-iso-datetime "2015-10-13T02:00:00.000Z"),
                                           :carrier            "HV",
                                           :flightNumber       626,
                                           :destinationAirport "AMS",
                                           },
                 :airItineraryPricingInfo {
                                           :currencyCode            "EUR",
                                           :baseFare                207.01,
                                           :taxSurcharge            8.99,
                                           :totalPriceOnePassenger  216,
                                           :totalPriceAllPassengers 216,
                                           :bookingFee              0
                                           }
                 :timestamp               2
                 })


(defn db-test []
      (let [conn (mg/connect {:host "localhost" :port 27017})]
           (mg/get-db conn "wcig-test")))




(binding [db (atom (db-test))]
         (let [outdate (parse-std-local-date "2015-10-05")
               indate (parse-std-local-date "2015-10-12")
               _ (mc/drop @db db/AIRPORTS)
               _ (save-airport {:code "XXX"
                                :tz   {
                                       :dstOffset    0,
                                       :rawOffset    3600,
                                       :status       "OK",
                                       :timeZoneId   "Asia/Tokyo",
                                       :timeZoneName "Japan Standard Time"
                                       }})
               _ (save-airport {:code "YYY"
                                :tz   {
                                       :dstOffset    0,
                                       :rawOffset    7200,
                                       :status       "OK",
                                       :timeZoneId   "Asia/Tokyo",
                                       :timeZoneName "Japan Standard Time"
                                       }})
               ]
              (fact "finds time difference between two airports"
                    (airports-time-dif "XXX" "YYY") => 3600
                    (airports-time-dif "YYY" "XXX") => -3600
                    (airports-time-dif "XXX" "XXX") => 0
                    )))












(ns wcig.listeners.q-test
  (:require [clojure.test :refer :all]
            [wcig.dbmemo :refer :all]
            [wcig.listeners.q :refer :all]
            [wcig.util :refer :all]
            [midje.sweet :refer :all]
            [clojure.data.json :as json]
            ))


(def itineraries-record
  {
   :origin      "AMS"
   :outDate     "2015-11-20"
   :destination "DXB",
   :group       :qpx
   :inDate      "2015-11-22"
   :itineraries [
                 {
                  :currency             "EUR",
                  :price                420,
                  :outDepartureDateTime (parse-iso-datetime "2015-11-20T12:20:00.000Z"),
                  :outArrivalDateTime   (parse-iso-datetime "2015-11-21T02:00:00.000Z"),
                  :inDepartureDateTime  (parse-iso-datetime "2015-11-22T08:25:00.000Z"),
                  :inArrivalDateTime    (parse-iso-datetime "2015-11-22T17:10:00.000Z"),
                  :inDuration           705,
                  :outDuration          640,
                  :slice
                                        [
                                         [
                                          {:aircraft           "332",
                                           :departureTime      (parse-iso-datetime "2015-11-20T12:20:00.000Z"),
                                           :arrivalTime        (parse-iso-datetime "2015-11-20T16:50:00.000Z"),
                                           :carrier            "TK",
                                           :connectionDuration 165,
                                           :destination        "IST",
                                           :duration           210,
                                           :number             "1952",
                                           :origin             "AMS"
                                           }
                                          {:aircraft      "343",
                                           :arrivalTime   (parse-iso-datetime "2015-11-21T02:00:00.000Z")
                                           :carrier       "TK",
                                           :departureTime (parse-iso-datetime "2015-11-20T19:35:00.000Z"),
                                           :destination   "DXB",
                                           :duration      265,
                                           :number        "760",
                                           :origin        "IST"}
                                          ]
                                         [
                                          {:aircraft           "330",
                                           :arrivalTime        (parse-iso-datetime "2015-11-22T11:40:00.000Z"),
                                           :carrier            "TK",
                                           :connectionDuration 165,
                                           :departureTime      (parse-iso-datetime "2015-11-22T08:25:00.000Z"),
                                           :destination        "IST",
                                           :duration           315,
                                           :number             "763",
                                           :origin             "DXB"}
                                          {:aircraft      "321",
                                           :arrivalTime   (parse-iso-datetime "2015-11-22T17:10:00.000Z"),
                                           :carrier       "TK",
                                           :departureTime (parse-iso-datetime "2015-11-22T14:25:00.000Z"),
                                           :destination   "AMS",
                                           :duration      225,
                                           :number        "1953",
                                           :origin        "IST"}]]}
                 {:currency             "EUR",
                  :inArrivalDateTime    (parse-iso-datetime "2015-11-22T08:15:00.000Z"),
                  :inDepartureDateTime  (parse-iso-datetime "2015-11-22T01:50:00.000Z"),
                  :inDuration           565,
                  :outArrivalDateTime   (parse-iso-datetime "2015-11-20T22:35:00.000Z"),
                  :outDepartureDateTime (parse-iso-datetime "2015-11-20T10:55:00.000Z"),
                  :outDuration          520,
                  :price                465,
                  :slice                [[{:aircraft           "319",
                                           :arrivalTime
                                                               (parse-iso-datetime "2015-11-20T12:00:00.000Z"),
                                           :carrier            "LH",
                                           :connectionDuration 80,
                                           :departureTime      (parse-iso-datetime "2015-11-20T10:55:00.000Z"),
                                           :destination        "FRA",
                                           :duration           65,
                                           :number             "989",
                                           :origin             "AMS"}
                                          {:aircraft      "744",
                                           :arrivalTime   (parse-iso-datetime "2015-11-20T22:35:00.000Z"),
                                           :carrier       "LH",
                                           :departureTime (parse-iso-datetime "2015-11-20T13:20:00.000Z"),
                                           :destination   "DXB",
                                           :duration      375,
                                           :number        "630",
                                           :origin        "FRA"}
                                          ]
                                         [{:aircraft           "744",

                                           :arrivalTime        (parse-iso-datetime "2015-11-22T05:40:00.000Z"),
                                           :carrier            "LH",
                                           :connectionDuration 80,
                                           :departureTime      (parse-iso-datetime "2015-11-22T01:50:00.000Z"),
                                           :destination        "FRA",
                                           :duration           410,
                                           :number             "631",
                                           :origin             "DXB"}
                                          {:aircraft      "32A",
                                           :arrivalTime   (parse-iso-datetime "2015-11-22T08:15:00.000Z"),
                                           :carrier       "LH",
                                           :departureTime (parse-iso-datetime "2015-11-22T07:00:00.000Z"),
                                           :destination   "AMS",
                                           :duration      75,
                                           :number        "986",
                                           :origin        "FRA"}
                                          ]]
                  }
                 ]

   }
  )

(fact "qpx converter"
      (let [rec (json/read-str (slurp "test/wcig/listeners/qpx-1.js") :key-fn keyword)]

        (make-qpx-itinerary rec) => itineraries-record
        (let [splitted (split-itineraries itineraries-record)]
          (count splitted) => 2
          (count (:itineraries (first splitted))) => 2
          ;   (count (:id_slices (first splitted))) => 36       ; uuid
          ;   (count (second splitted)) => 2
          ;   (count (second splitted)) => 2
          )
        ))

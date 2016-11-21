(ns wcig.service-test
  (:require
    [clojure.test :refer :all]
    [clojure.pprint :refer :all]
    [wcig.db :as db]
    [wcig.dbbase :refer :all]
    [wcig.util :refer :all]
    [midje.sweet :refer :all]
    [monger.core :as mg]
    [monger.collection :as mc]
    [wcig.db :as db]
    [wcig.service :refer :all]

    ))


(def its-record {

                 :origin      "AMS"
                 :outDate     "2015-11-24"
                 :destination "XXX"
                 :inDate      "2015-11-25"

                 :itineraries [
                               {
                                :carriers             ["KL"]

                                :outDepartureDateTime (parse-iso-datetime "2015-07-24T10:00:00.000Z")
                                :outArrivalDateTime   (parse-iso-datetime "2015-07-24T13:00:00.000Z")
                                :outDuration          375

                                :inDepartureDateTime  (parse-iso-datetime "2015-07-25T17:00:00.000Z")
                                :inArrivalDateTime    (parse-iso-datetime "2015-07-25T20:00:00.000Z")
                                :inDuration           980

                                :price                533
                                :direct               false
                                :slices               "s1"
                                }
                               {
                                :carriers             ["KL"]

                                :outDepartureDateTime (parse-iso-datetime "2015-07-24T10:00:00.000Z")
                                :outArrivalDateTime   (parse-iso-datetime "2015-07-24T13:00:00.000Z")
                                :outDuration          375

                                :inDepartureDateTime  (parse-iso-datetime "2015-07-25T17:00:00.000Z")
                                :inArrivalDateTime    (parse-iso-datetime "2015-07-25T20:00:00.000Z")
                                :inDuration           980

                                :price                533
                                :direct               false
                                :slices               "s2"
                                }
                               ]
                 })

(defn db-test []
  (let [conn (mg/connect {:host "localhost" :port 27017})]
    (mg/get-db conn "wcig-test")))



(binding [db (atom (db-test))]
  (let [
        _ (mc/drop @db db/LATEST-ITINERARY)
        _ (db/upsert-itineraries its-record)]
    (fact "find-itineraries"
          (count (db/find-itineraries "AMS" "2015-11-24" "XXX" "2015-11-25")) => 1
          (count (db/find-itineraries "AMS" "2015-01-01" "XXX" "2015-11-25")) => 0
          )
    ))

(fact "recognizes mobile browsers"
      (is-mobile-browser? {:headers {"user-agent" "Mozilla"}}) => falsey
      (is-mobile-browser? {:headers {"user-agent"
                                  (str "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K)"
                                       " AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")}}) => truthy
      (is-mobile-browser? {:headers {"user-agent"
                                  (str "Mozilla/5.0 (Linux; Android 5.1.1; SM-G920F Build/LMY47X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.84 Mobile Safari/537.36")}}) => truthy
      )



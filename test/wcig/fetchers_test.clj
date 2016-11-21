(ns wcig.fetchers_test
  (:require
    [clojure.test :refer :all]
    [wcig.fetchers :refer :all]
    [wcig.util :refer :all]
    [midje.sweet :refer :all]
    ))

(def wo1 (parse-std-local-date "2015-09-23"))
(def vr1 (parse-std-local-date "2015-09-25"))
(fact "days of week ok"
      (day-of-week wo1) => 3
      (day-of-week vr1) => 5
      )


(fact "days between ok"
      (days-between wo1 vr1) => 2
      (days-between vr1 vr1) => 0
      )


(fact "Needs renewal from saturday"
      (needs-renewal-existing (parse-iso-datetime "2015-09-23T07:00.00Z")
                              100
                              ["AMS" "2015-09-23" "VIE" "2015-09-25"]
                              {:max-age 101})
      => false
      (needs-renewal-existing (parse-iso-datetime "2015-09-23T07:00.00Z")
                              100
                              ["AMS" "2015-09-23" "VIE" "2015-09-25"]
                              {:max-age 99})
      => true)


(def test-holidays [
                    {:id         "MEIVD" :name "Meivakantie ASVO"
                     :start-date "2016-04-23" :end-date "2016-05-07"
                     :groups     #{"holiday" }
                     }
                    {
                     :id         "MEIV" :name "Meivakantie"
                     :start-date "2016-04-30" :end-date "2016-05-07"
                     :groups     #{"holiday"}
                     }
                    {
                     :id         "KRV" :name "Krokusvakantie"
                     :start-date "2016-02-20" :end-date "2016-02-27"
                     :groups     #{"holiday"}
                     }
                    {
                     :id         "KRVN" :name "Krokusvakantie Noord"
                     :start-date "2016-02-27" :end-date "2016-03-05"
                     :groups     #{"holiday"}
                     }
                    {
                     :id         "SOM1" :name "Zomervakantie 1"
                     :start-date "2016-07-09" :end-date "2016-07-23"
                     :groups     #{"transavia" "holiday"}
                     }
                    {
                     :id         "SOM2" :name "Zomervakantie 2"
                     :start-date "2016-07-16" :end-date "2016-07-30"
                     :groups     #{"transavia" "holiday"}
                     }
                    {
                     :id         "SOM3" :name "Zomervakantie 2"
                     :start-date "2016-07-23" :end-date "2016-09-06"
                     :groups     #{"transavia" "holiday"}
                     }

                    ])

(fact "fetching relevant data"
      (count (get-relevant-holidays "holiday" (parse-std-local-date "2015-01-01") test-holidays)) => 7
      (count (get-relevant-holidays "holiday" (parse-std-local-date "2017-01-01") test-holidays)) => 0
      (count (get-relevant-holidays "xx" (parse-std-local-date "2015-01-01") test-holidays)) => 0
      (count (get-relevant-holidays "transavia" (parse-std-local-date "2015-01-01") test-holidays)) => 3
      (count (get-relevant-holidays "holiday" (parse-std-local-date "2016-06-01") test-holidays)) => 3

      )
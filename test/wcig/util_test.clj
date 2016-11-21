(ns wcig.util-test
    (:require [clojure.test :refer :all]
      [wcig.util :refer :all]
      [clojure.test :refer :all]
      [midje.sweet :refer :all]))

(fact "standard time conversions"

      (let [
            dt-tz-2 (parse-iso-datetime "2015-07-17T22:05:40.140+02:00")
            dt-tz-0 (parse-iso-datetime "2015-07-17T20:05:40.140Z")

            winter-instant (.getMillis (parse-iso-datetime "2015-01-01T00:00:00.000"))
            summer-instant (.getMillis (parse-iso-datetime "2015-07-01T00:00:00.000"))
            ]

           (.getMillis dt-tz-2) => (.getMillis dt-tz-0)     ; same instant
           (-> dt-tz-2 .getChronology .getZone .toString) => "+02:00"
           (-> dt-tz-0 .getChronology .getZone .toString) => "UTC"

           (-> dt-tz-2 .getChronology .getZone (.getOffset winter-instant)) => (* 2 60 60 1000)
           (-> dt-tz-2 .getChronology .getZone (.getOffset summer-instant)) => (* 2 60 60 1000)
           ))

(fact "Conversion to localdatetime"
      (let [
            dt-22-tz2 (parse-iso-datetime "2015-07-17T22:05:40.140+02:00")
            dt-22-UTC (parse-iso-datetime "2015-07-17T22:05:40.140Z")
            ]
           (to-local-datetime-in-utc dt-22-tz2) => dt-22-UTC
           )
      )

(fact "To airport time converstion"
      (to-airport-time "2015-07-17T22:05:40.140+02:00")
      =>
      {:local-as-utc (parse-iso-datetime "2015-07-17T22:05:40.140Z")
       :utc          (parse-iso-datetime "2015-07-17T20:05:40.140Z")}
      )

(fact "elements occuring mor than..."
      (set (elements-occuring-more-than [1 2 3 1 2] 0)) => #{1 2 3}
      (set (elements-occuring-more-than [1 2 3 1 2] 1)) => #{1 2}
      (set (elements-occuring-more-than [1 2 3 1 2 1] 2)) => #{1}
      )




(fact "flight diration between timezones"
      ; flight from AMS to LON
      (flight-duration-with-tz
        (parse-iso-datetime "2015-07-17T22:05:40.140Z")
        (parse-iso-datetime "2015-07-17T22:05:40.140Z")
        -3600) => 60
      (flight-duration-with-tz
        (parse-iso-datetime "2015-07-17T22:05:40.140Z")
        (parse-iso-datetime "2015-07-17T23:05:40.140Z")
        -3600) => 120
      )




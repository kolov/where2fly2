(ns wcig.search
  (:require [clj-time.core :as t]
            [wcig.db :as db]
            [wcig.data :as data]
            [wcig.util :refer :all]
            )
  (:import (org.joda.time LocalDate DateTime)))

(def WEEKENDS-AHEAD 8)
(def ^:private days-till-friday [0 4 3 2 1 7 6 5])

(defn next-friday []
  (let [td (t/today)]
    (.plusDays td (get days-till-friday (.getDayOfWeek td)))))

(defn offset-days [[^LocalDate out-date ^LocalDate in-date] d1 d2]
  [(.plusDays out-date d1) (.plusDays in-date d2)])

(defn next-weekend
  "returns next weekend days with offsets: firiday +d1, sunday +d2"
  ([d1 d2]
   (let [friday (next-friday)]
     [(.plusDays friday d1)
      (.plusDays friday (+ 2 d2))
      ]))
  ([] (next-weekend 0 0)))

(defn coming-weekends
  ([_ first-weekend]
   (let [weekends-as-date
         (for [n (range WEEKENDS-AHEAD)] (map #(.toDateTimeAtStartOfDay %) (offset-days first-weekend (* n 7) (* n 7))))]
     (for [w weekends-as-date] {:out (unparse-std-date (first w)) :in (unparse-std-date (second w))})
     ))
  ([origin] (coming-weekends origin (next-weekend))))

(defn coming-holidays
  ([groups start-date]
   (let [result (filter
                  (fn [h] (-> h :start-date parse-std-local-date to-beginning-day (.isAfter start-date)))
                  data/holidays)
         _ (println result)
         result (filter
                  (fn [h] (some #(= % groups) (:groups h))) result)
         _ (println result)
         ]
     result))
  ([groups] (coming-holidays groups (DateTime.))))


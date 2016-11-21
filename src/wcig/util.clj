(ns wcig.util
  (:require
    [clj-time.format :as tf]
    [clj-time.core :as t]
    [clojure.string :as str]
    )
  (:import (org.joda.time.format ISODateTimeFormat)
           (org.joda.time LocalDate DateTime DateTimeZone)))

(defn now [] (System/currentTimeMillis))

(def ^:private standard-date-format
  "dat format used in API, URLs etc"
  (.withOffsetParsed (tf/formatter "yyyy-MM-dd")))

(def flight-time-format
  "format used to show flight time"
  (tf/formatter "yyyy-MM-dd HH:mm"))

(defprotocol some-time
  (to-beginning-day [this])
  (to-end-day [this])
  )

(extend-protocol some-time
  LocalDate
  (to-beginning-day [this] (.toDateTimeAtStartOfDay this))
  (to-end-day [this] (.withTime (.toDateTimeAtStartOfDay this) 23 59 59 999))
  DateTime
  (to-beginning-day [this] (.withTime this 0 0 0 0))
  (to-end-day [this] (.withTime this 23 59 59 999))
  )


(defn parse-std-local-date [s] (tf/parse-local-date standard-date-format s))
(defn unparse-std-local-date [^LocalDate ldt] (tf/unparse-local-date standard-date-format ldt))
(defn unparse-std-date [^DateTime dt] (tf/unparse standard-date-format dt))


(def iso-datetime-parser (.withOffsetParsed (ISODateTimeFormat/dateTimeParser)))

(defn parse-iso-localdatetime [s] (.parseLocalDateTime iso-datetime-parser s))
(defn parse-iso-localdate [s] (.parseLocalDate iso-datetime-parser s))
(defn parse-iso-datetime [s] (.parseDateTime iso-datetime-parser s))


(defrecord airport-time [utc local-as-utc])

(defmulti to-local-datetime-in-utc class)

(defmethod to-local-datetime-in-utc DateTime [dt]
  (let [tz-offset (-> dt .getChronology .getZone (.getOffset (.getMillis dt)))]
    (-> dt (.plusMillis tz-offset) (.toDateTime DateTimeZone/UTC))
    ))

(defmethod to-local-datetime-in-utc String [iso]
  (to-local-datetime-in-utc (parse-iso-datetime iso)))

(defn to-airport-time [^String iso]
  (let [dt (parse-iso-datetime iso)]
    (->airport-time (.toDateTime dt DateTimeZone/UTC) (to-local-datetime-in-utc dt))))

(defn elements-occuring-more-than [a limit]
  "[ [1 2] [1 2] [2 3] [3 4] [1 2]] => {[3 4] 1, [2 3] 1, [1 2] 3}"
  (let [grouped (zipmap (distinct a) (map #(count (filter #{%} a)) (distinct a)))]
    (for [[k v] grouped :when (> v limit)] k)))

(defn parse-around[s]
  (if-let [m (re-find #"^(.)d" s)]
    (let[ days (Integer/parseInt (second m))]
      (range (- days) (inc days)))
    [0]))

(defn expand-day [^String date ^String around]
  "Return array of days expanded with the value of around"
  (let [day (parse-iso-localdate date)]
    (for [i (parse-around around)] (.plusDays day i))))

(defn make-search-dates [^String out-date ^String in-date around]
  "create dat combinations from out-data, in-date, params. Eg: 10-10, 15-10, {:around :day } =>
    [9-10 14-10], [9-10 15-10=...
  "
  (for [out (expand-day out-date around)
        in (expand-day in-date around)] [out in]))

(defn parse-cs-strings [ss]
  "parse comma separated sting"
  (if (not (str/blank? ss))
    (str/split ss #"\,")))

(defn flight-duration-with-tz [^DateTime lt1 ^DateTime lt2 dif]
  {:pre [(= DateTime (class lt1)) (= DateTime (class lt2)) dif]}
  (let [dt (- (.getMillis lt2) (.getMillis lt1))
        dt (-> dt (/ 1000) (- dif) (/ 60))
        ]
    (int dt)))

(defmethod print-method DateTime [x w] (.write w (.toString x)))
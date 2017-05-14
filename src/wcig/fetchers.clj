(ns wcig.fetchers
  (:require
    [wcig.db :as db]
    [wcig.core :as core]
    [wcig.connectors.lh :as lh]
    [wcig.util :refer :all]
    [wcig.data :as data]
    [slingshot.slingshot :refer [throw+]]
    [taoensso.timbre :as log]
    [wcig.search :as search])
  (:import (org.joda.time LocalDate DateTime Days Period)
           )
  )


(defn weekend-offsets [skip-weeks weeks]
  "return week offset for the following year"
  (for [wk (range (* 7 skip-weeks) (* 7 weeks) 7)
        wk-offsets [
                    #_fri-sun [0 0]
                    #_fri-mon [0 1]

                    #_thu-sun [-1 0]
                    #_thu-mon [-1 1]


                    #_sat-mon [1 1]
                    ]
        ]
    [(+ (first wk-offsets) wk) (+ (second wk-offsets) wk)]))

(def holiday-offsets
  "return holiday offset for the following year"
  [
   [0 0]
   [0 -1]
   [0 -2]
   [0 1]

   [-1 0]
   [-1 -1]
   [-1 -2]
   [-1 1]

   [1 0]
   [1 -1]
   [1 -2]
   [1 1]

   [2 0]
   [2 -1]
   [2 -2]
   [2 1]
   ])

(defn next-dates [out-date in-date offsets]
  (map (fn [[out-offset in-offset]]
         [
          (unparse-std-local-date (.plusDays out-date out-offset))
          (unparse-std-local-date (.plusDays in-date in-offset))
          ]
         ) offsets)
  )

(defn next-weekend-requests [origin dates dests]
  (for [[out in] dates dest dests :when (not= origin dest)]
    [origin out dest in]))

(defn day-of-week [^LocalDate ldt]
  (-> ldt .dayOfWeek .getAsString Integer/parseInt))

(defn days-between [^LocalDate d1 ^LocalDate d2]
  (.getDays (Days/daysBetween d1 d2)))


(defn needs-renewal-existing [^DateTime dt age-hours qpx-request params]
  {:pre [(:max-age params) {:limit params}]}
  (log/info "needs renewal: age-hours=" age-hours ", (:max-age params)=" (:max-age params))
  (let [out-date (parse-std-local-date (nth qpx-request 1))
        in-date (parse-std-local-date (nth qpx-request 3))
        days-from-now (days-between (.toLocalDate dt) out-date)]
    (> age-hours (:max-age params))))


(defn needs-renewal [qpx-request params]
  {:pre [(:max-age params) {:limit params}]}
  (if-let [existing (db/find-qpx-call qpx-request)]
    (needs-renewal-existing (DateTime.)
                            (-> (System/currentTimeMillis) (- (:timestamp existing)) (/ 3600000) int)
                            qpx-request
                            params)
    true))


(defn fetch-next-itineraries
  [origin dates dests params]
  {:pre [(:max-age params) (:limit params)]}
  (let [fetched (db/find-qpx-calls-today)
        left-to-fetch (max 0 (- (:limit params) fetched))
        reqs (next-weekend-requests origin dates dests)]
    (log/info "Will fetch " left-to-fetch " itineraries from " (count reqs) ", already fetched " fetched "
           today")
    (loop [fetched 0
           reqs reqs]
      (do
        (when (and (seq reqs) (< fetched left-to-fetch))
          (let [req (first reqs)]
            (if (needs-renewal req params)
              (do
                (apply core/get-qpx-itineraries req)
                (db/save-qpx-call req)
                (log/info "Fetched " req)
                (recur (inc fetched) (next reqs)))
              (do
                (log/info "Will not refresh " req)
                (recur fetched (next reqs)))
              ))))
      )))

(defn fetch-weekend-itineraries
  ([origin skip-weeks weeks params]
   (let [fri (search/next-friday)
         dates (next-dates fri (.plusDays fri 2) (weekend-offsets skip-weeks weeks))]
     (fetch-next-itineraries origin dates data/europe-airports params)))
  ([origin weeks params] (fetch-weekend-itineraries origin weeks 0 params))
  )


(defn fetch-weekends-till-limit
  ([origin weeks limit] (fetch-weekend-itineraries origin weeks {:max-age 50000 :limit limit}))
  ([origin limit] (fetch-weekends-till-limit origin 4 limit))
  ([origin] (fetch-weekends-till-limit origin 4 50))
  )


(defn fetch-holiday-itineraries-at-dates [origin ^String start-date ^String end-date params]
  {:pre [(:max-age params) (:limit params) start-date start-date]}
  (fetch-next-itineraries origin
                          (next-dates
                            (parse-std-local-date start-date)
                            (parse-std-local-date end-date)
                            holiday-offsets)
                          data/holiday-airports params))

(defn get-relevant-holidays [groups query-date holidays-data]
  (->> holidays-data
       (filter #(contains? (:groups %) groups))

       (filter #(< 1 (.getDays (Days/daysBetween query-date (parse-std-local-date (:start-date %)))))
               )))

(defn fetch-holiday-itineraries [origin params]
  {:pre [(:max-age params) (:limit params) (:groups params)]}
  (doall (map #(fetch-holiday-itineraries-at-dates origin (:start-date %) (:end-date %) params)
              (get-relevant-holidays (:groups params) (LocalDate.) data/holidays))))


; transavia


; to fetch all holidays
; validate: (fetch-holiday-itineraries "AMS" {:max-age 168 :limit 110})
; (fetch-holiday-itineraries "AMS" {:max-age 100 :limit 2 :groups "holiday"})
; (tr/update-transavia-itinerarie "AMS" 2015 10)


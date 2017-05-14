(ns wcig.jobs
  (:import [org.joda.time DateTimeZone DateTime])
  (:require
    [chime :refer [chime-at]]
    [clj-time.core :as t]
    [clj-time.periodic :refer [periodic-seq]]

    [wcig.core :as core]
    [wcig.fetchers :as fetchers]))

(def QPX_LIMIT 50)
(def TZ_AMSTERDAM (DateTimeZone/forID "Europe/Amsterdam"))

(defn dayly-at [h m]
  (periodic-seq (-> (t/now)
                    (.withZone TZ_AMSTERDAM)
                    (.withTime h m 0 0))
                (t/days 1)
                ))

(defn on-days [hour minute & days]
  (let [y (-> (t/now) .getYear)]
    (for [year (iterate inc y) month (range 1 13)  day days ]
      (DateTime. year month day hour minute 0 0 TZ_AMSTERDAM)
           )))

(defn schedule-jobs []
  (chime-at (on-days 5 15 25) (fn [t] (core/update-transavia-routes)))
  (chime-at (dayly-at 8 15) (fn [t] (core/update-transavia-flightsinfo)))
  (chime-at (dayly-at 7 15)
            (fn [t]
              (comment "Updates QPX holiday filights")
              (fetchers/fetch-holiday-itineraries "AMS" {:max-age 470 :limit QPX_LIMIT :groups "holiday"})
              (fetchers/fetch-holiday-itineraries "AMS" {:max-age 200 :limit QPX_LIMIT :groups "holiday"})
              (fetchers/fetch-holiday-itineraries "AMS" {:max-age 100 :limit QPX_LIMIT :groups "holiday"})
              (fetchers/fetch-holiday-itineraries "AMS" {:max-age 50 :limit QPX_LIMIT :groups "holiday"})
              (fetchers/fetch-holiday-itineraries "AMS" {:max-age 20 :limit QPX_LIMIT :groups "holiday"}))
            {:on-finished (fn [] (println "Retrieved QPX flights ."))})

  ) 
  

(ns wcig.jobs
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.schedule.cron :refer [schedule cron-schedule]]
            [wcig.core :as core]
            [wcig.fetchers :as fetchers]
            [wcig.data :as data]))


(defjob update-transavia-routes-job [ctx]
        (comment "Updates transavia routes (destionations from origin)")
        (core/update-transavia-routes)
        )

(defjob update-transavia-flights-job [ctx]
        (comment "Updates transavia filight info22")
        (core/update-transavia-flightsinfo)
        )

(defjob update-qpx-holidays-job [ctx]
        (comment "Updates QPX holiday filights")
        (fetchers/fetch-holiday-itineraries "AMS" {:max-age 470 :limit 50 :groups "holiday"})
        (fetchers/fetch-holiday-itineraries "AMS" {:max-age 200 :limit 50 :groups "holiday"})
        (fetchers/fetch-holiday-itineraries "AMS" {:max-age 100 :limit 50 :groups "holiday"})
        (fetchers/fetch-holiday-itineraries "AMS" {:max-age 50 :limit 50 :groups "holiday"})
        (fetchers/fetch-holiday-itineraries "AMS" {:max-age 20 :limit 50 :groups "holiday"})
        )

(defonce scheduler (atom nil))

(def few-times-a-month
  "schedule that fires 3 times a month"
  (t/build
    (t/with-identity (t/key "triggers.few-times-a-month"))
    (t/start-now)
    (t/with-schedule (schedule
                       (cron-schedule "0 0 8 5,15,25 * ?")))
    ))


(defn start-jobs []
  (if-not @scheduler (do
                       (reset! scheduler (-> (qs/initialize) qs/start))
                       (qs/schedule @scheduler (j/build
                                                 (j/of-type update-transavia-routes-job)
                                                 (j/with-identity (j/key "update-transavia-routes-job")))
                                    few-times-a-month)
                       (qs/schedule @scheduler (j/build
                                                 (j/of-type update-transavia-flights-job)
                                                 (j/with-identity (j/key "update-transavia-flights-job")))
                                    (t/build
                                      (t/with-identity (t/key "triggers.tr-every-morning"))
                                      (t/start-now)
                                      (t/with-schedule (schedule
                                                         (cron-schedule "0 15 10 * * ? *")))
                                      ))
                       (qs/schedule @scheduler (j/build
                                                 (j/of-type update-qpx-holidays-job)
                                                 (j/with-identity (j/key "update-qpx-holidays-job")))
                                    (t/build
                                      (t/with-identity (t/key "triggers.qpx-every-morning"))
                                      (t/start-now)
                                      (t/with-schedule (schedule
                                                         (cron-schedule "0 45 10 * * ? *")))
                                      ))

                       ))
  )

(defn pause-jobs []
  (qs/standby @scheduler))

(defn resume-jobs []
  (qs/start @scheduler))

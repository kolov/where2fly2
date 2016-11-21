(ns wcig.connectors.klm
  (:require [org.httpkit.client :as client]
            [confiture.core :refer [value]]
            [slingshot.slingshot :refer [throw+]]
            [wcig.connectors.base :refer :all]
            ))

(defn get-klm [url]
  (client/get url {:headers {"Authorization" (value :klm-)}} parse-response)
  )

(defn get-weather-async [code]
  (client/get (str "/travel/locations/v2/cities/" code "/weather") {} parse-response))

(defn get-weather-sync [code]
  @(get-weather-async code))




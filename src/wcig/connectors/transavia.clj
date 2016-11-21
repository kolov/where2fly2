(ns wcig.connectors.transavia
  (:require [org.httpkit.client :as client]
            [confiture.core :refer [value]]  
            [slingshot.slingshot :refer [throw+]]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [wcig.connectors.base :refer :all]
            )
  (:import (org.joda.time DateTime)))


(defn get-transavia [url]
  (client/get url {:headers {"apikey" (value :transavia-key)}} parse-response)
  )
;-- GET ROUTES

(def routes-url "https://api.transavia.com/v1/routes")
(defn get-routes [] (:routes @(get-transavia routes-url)))

(defn pr-month [s]
  "prints month as 2 digits"
  (String/format "%02d" (into-array Integer [(int s)])))

(def ^:private api-date-format
  "dat format used in API, URLs etc"
  (.withOffsetParsed (tf/formatter "yyyy-MM-dd")))
(defn unparse-api-date [^DateTime dt] (tf/unparse api-date-format dt))



(defn get-availability-async [origin dest year month]
  (get-transavia (str "https://api.transavia.com/v1/metasearch/availability/"
                      origin "-" dest "/pax/1/month/" year "-" (pr-month month))))

(defn get-availability [origin dest year month] @(get-availability-async origin dest year month))

(defn get-flight-offers-month [origin dest year month]
  @(get-transavia
     (str "https://api.transavia.com/v1/flightoffers?origin=" origin
          "&destination=" dest
          "&origindeparturedate=" year (pr-month month))))

(defn get-flight-offers-day [origin dest out in]
  @(get-transavia
     (str "https://api.transavia.com/v1/flightoffers?origin=" origin
          "&destination=" dest
          "&origindeparturedate=" (unparse-api-date out)
          "&destinationdeparturedate=" (unparse-api-date in)
          )))


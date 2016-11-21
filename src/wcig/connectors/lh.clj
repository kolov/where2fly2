(ns wcig.connectors.lh
  (:require [org.httpkit.client :as client]
            [confiture.core :refer [value]]
            [slingshot.slingshot :refer [throw+]]
            [wcig.connectors.base :refer :all]
            ))

(def GET-TOKEN-URL "https://api.lufthansa.com/v1/oauth/token")
(def GET-AIRPORTS-URL "https://api.lufthansa.com/v1/references/airports")

(defn get-token-async []
  (client/post GET-TOKEN-URL
               {
                :headers {"Content-Type" "application/x-www-form-urlencoded"}
                :body    (str "client_id=" (value :lh-key)
                              "&client_secret=" (value :lh-secret)
                              "&grant_type=client_credentials")

                } parse-response))

(def token (atom nil))
(defn get-token []
  (if (and (:access_token @token) (> (:valid_till @token) (System/currentTimeMillis)))
    (:access_token @token)
    (let [x @(get-token-async)]
      (reset! token (assoc x :valid_till (+ (* 1000 (:expires_in x)) (System/currentTimeMillis))))
      (:access_token x))
    ))


(defn authorize [req] (update-in req [:headers] assoc "Authorization" (str "Bearer " (get-token))))

(defn get-airport [a]
  @(client/get (str GET-AIRPORTS-URL "/" a)
               (authorize {})
               parse-response))

(defn get-airports []
  @(client/get (str GET-AIRPORTS-URL)
               (authorize {})
               parse-response))

(defn parse-airports [resp]
  (->> resp :AirportResource :Airports :Airport
       (map
         (fn [x]
           (let [names (-> x :Names :Name)
                 names (if (map? names) [names] names)
                 names (map (fn [n] {:lang ((keyword "@LanguageCode") n) :val ((keyword "$") n)}) names)]
             {:code         (:AirportCode x)
              :location     {
                             :lat (-> x :Position :Coordinate :Latitude)
                             :lng (-> x :Position :Coordinate :Longitude)
                             }
              :city-code    (:CityCode x)
              :country-code (:CountryCode x)
              :long_name    (-> (filter (fn [n] (= "en" (:lang n))) names) first :val)
              })))))


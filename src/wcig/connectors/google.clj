(ns wcig.connectors.google
  (:require [org.httpkit.client :as client]
            [confiture.core :refer [value]]
            [slingshot.slingshot :refer [throw+]]
            [wcig.connectors.base :refer :all]
            ))

(def GEOCODING-URL "https://maps.googleapis.com/maps/api/geocode/json")
(def TIMEZONE-URL "https://maps.googleapis.com/maps/api/timezone/json")

(defn get-geocode-async [s]
  (client/get (str GEOCODING-URL "?key=" (value :google-geocoding-key)
                   "&address=" s) {} parse-response))
(defn get-geocode-sync [s] @(get-geocode-async s))

(defn has-type [loc type]
  "True id a location returned by google maps has this type"
  (some #(= type %) (:types loc)))

(defn geocode-to-location [code gc]
  (let [address-components (:address_components gc)
        name-component (first (filter #(has-type % "point_of_interest") address-components))
        name-component (if name-component name-component (first address-components))
        name (:long_name name-component)
        name (if name name code)]
    {:location  (get-in gc [:geometry :location])
     :place_id  (:place_id gc)
     :code      code
     :long_name name
     }))


(defn fetch-airport [code]
  (let [code-for-google (get {"RTM" "Rotterdam"
                              "MST" "Maastricht"
                              "OLB" "costa+smeralda" "EFL" "Kefalonia" "CMF" "Chambery"
                              "OPO" "Porto" "PFO" "Paphos"
                              "FNC" "Funchal" "RAK" "Marakech"
                              "BVC" "Boa+Vista"
                              "PMO" "Palermo" "NBE" "Enfidha" "AGP" "Malaga"
                              "RHO" "Rhodos" "PVK" "Preveza"
                              "SKG" "Thessaloniki" "DXB" "Dubai+International"
                              "FAO" "Faro" "NCE" "Nice"
                              "PSA" "Pisa"
                              "KLX" "Kalamata"
                              "SID" "Amilcar Cabral International"
                              "EIN" "Eindhoven"} code code)]
    (->> (get-geocode-sync (str code-for-google "+airport"))
         (:results)
         (filter (fn [loc] (some #(= "airport" %) (:types loc))))
         (first)
         (geocode-to-location code))))

(defn get-timezone [lat lng]
  (let [url (str TIMEZONE-URL "?"
                 "location=" lat "," lng
                 "&timestamp=" (int (/ (System/currentTimeMillis) 1000))
                 "&key=" (value :google-geocoding-key)
                 )
        ]
    (println url)
    @(client/get url {} parse-response)))
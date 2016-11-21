(ns wcig.view
  (:require

    [wcig.util :refer :all]
    [wcig.db :as db]
    [slingshot.slingshot :refer [throw+]]
    )
  )

(defn get-destinations [origin group]
  "Get destinations from origin airport using some source service"
  (println "!!group=" group)
  (cond (= (keyword group) :transavia) (db/get-transavia-destnations [origin])
        :default (throw+ (str "group is empty or unknown: " group))))

(defn get-flights-from [origin out-date {:keys [around group]}]
  (let [dates (expand-day out-date around)
        out-dates (map unparse-std-local-date dates)
        destinations (get-destinations origin group)
        flights (db/find-flights-from-on-dates origin out-dates destinations group)]
    (map #(dissoc % :_id) flights)
    ))

(defn get-flights-to [origin in-date {:keys [around group]}]
  (println "in-date" in-date)
  (let [dates (expand-day in-date around)
        in-dates (map unparse-std-local-date dates)
        destinations (get-destinations origin group)
        flights (db/find-flights-to-on-dates origin in-dates destinations group)]
    (map #(dissoc % :_id) flights)
    ))

(defn get-itineraries-from [origin out-date in-date {:keys [around group]}]
  (let [
        out-dates (map unparse-std-local-date (expand-day out-date around))
        in-dates (map unparse-std-local-date (expand-day in-date around))
        date-combinations (for [o out-dates i in-dates] [o i])
        itineraries (db/find-itineraries-from-on-dates origin date-combinations group)]
    (println "itineraries" itineraries)
    (map #(dissoc % :_id) itineraries)
    ))

(defn get-slices [origin out-date destination in-date group]
  (let [slices (db/find-slices-from-to-on-dates origin out-date destination in-date group)]
    (:slices slices)
    ))

(ns wcig.ref.init
  (:require
    [monger.collection :as mc]
    [wcig.db :as db]
    [wcig.dbbase :as dbbase]
    [wcig.connectors.google :as g]
    [clojure.edn :as edn]))


(def airports
  (edn/read-string (slurp (clojure.java.io/resource "airports.edn"))))

(defn enrich-airport-tz [a]
  (assoc a :tz (g/get-timezone (get-in a [:location :lat]) (get-in a [:location :lng]))))

(defn init-airports-with-tz []
  " obsolete"
  (mc/remove @dbbase/db db/AIRPORTS)
  (doseq [a airports]
    (do
      (mc/save @dbbase/db db/AIRPORTS (enrich-airport-tz a)))
    (println "Saved " (:code a))
    (Thread/sleep 120)
    ))

(defn update-airports-missing-tz []
  (doseq [a airports]
    (let [found (mc/find-one-as-map @dbbase/db db/AIRPORTS {:code (:code a)})
          tz (:tz found)]
      (when-not tz
        (mc/upsert @dbbase/db db/AIRPORTS {:code (:code a)} (enrich-airport-tz a))
        (println "Updated " (:code a))
        (Thread/sleep 120)
        ))))

(defn find-airport [code]
  (first
    (filter #(= code (:code %)) airports)))

; To initialize a DB:
; (init-airports-with-tz)



(ns wcig.connectors.base
  (:require
    [confiture.core :refer [value]]
    [clojure.data.json :as json]
    [slingshot.slingshot :refer [throw+]]
    [clj-time.format :as tf]
    )
  (:import (java.io InputStream)))

(defn read-stream [s]
  (let [b (byte-array 32000)
        n (.read s b)]
    (String. b 0 n)))


(def dt-formatter (tf/formatter "yyyy-MM-dd'T'HH:mm:ss"))
(defn parse-date [dt] (tf/parse dt-formatter dt))



(defmulti read-body (fn [body] (instance? InputStream body)))
(defmethod read-body true [body] (read-stream body))
(defmethod read-body false [body] body)

(defn parse-response [{:keys [status body] :as resp}]
  (cond
    (= status 200) (json/read-str (read-body body) :key-fn keyword)
    (= status 204) []
    :default (throw+ resp)))

(ns wcig.dbbase
  (:require
    [monger.conversion :refer [from-db-object]]
    [monger.core :as mg]
    [monger.operators :refer :all]
    [monger.joda-time]
    [jota.core :as log]
    [wcig.util :refer :all])
  (:import (org.joda.time DateTimeZone)
           ))

(DateTimeZone/setDefault DateTimeZone/UTC)

(defonce ^:dynamic db (atom nil))

(defn init [host port]
  (log/info "@db=" @db)
  (log/info "about to init" host ":" port)
  (let [conn (mg/connect {:host host :port port})]
    (log/info "connected to " host ":" conn)
    (reset! db (mg/get-db conn "wcig"))
    (log/info "@db=" @db)
    ))






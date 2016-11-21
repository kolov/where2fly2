(ns wcig.dbmemo
    (:require
      [monger.conversion :refer [from-db-object]]
      [monger.collection :as mc]
      [monger.query :as q]
      [monger.operators :refer :all]
      [monger.joda-time]
      [jota.core :as log]
      [wcig.util :refer :all]
      [wcig.dbbase :refer :all])
    )




(def CALL "call")

(defn hours [n] (* n 3600000))

(defn wrap-call [name f & args]
      (log/debug "wrap-call " name "," f "," args)
      (let [result (apply f args)]
           (log/debug "name:" name ", args: " args)
           (mc/save @db CALL
                    {:name name :timestamp (System/currentTimeMillis) :args args :result result})
           result))

(defn find-call [name & args]
      (let [last-item (first
                        (q/with-collection @db CALL
                                           (q/find {:name name :args args})
                                           (q/sort {:timestamp -1})
                                           (q/limit 1)))]
           (log/debug ":invalidated=" (:invalidated last-item))
           (if (:invalidated last-item) nil last-item)))



(defn invalidate-call [name]
      (mc/update @db CALL {:name name} {$set {:invalidated true}} {:multi true}))

(defn invalidate-all-calls []
      (mc/update @db CALL {} {$set {:invalidated true}} {:multi true}))


(defn age [record] (- (System/currentTimeMillis) (:timestamp record)))

(defn get-or-call [name maxage f & args]
      (log/debug get-or-call name args)
      (let [cached (apply find-call name args)]
           (log/debug (if cached (str "found one old " (- (System/currentTimeMillis) (:timestamp cached)) "ms") "Found none"))
           (if (and maxage cached (< (age cached) maxage))
             (do (log/debug "found in cache") (:result cached))
             (do (log/debug "not found in cache ") (apply wrap-call name f args)))))


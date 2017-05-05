(ns pulse.simple
  (:require [pulse.core :as pulse]

            [monger.conversion :refer [from-db-object]]
            [monger.collection :as mc]
            [monger.core :as mg]
            [monger.operators :refer :all]
            [monger.joda-time]
            [monger.query :as q]
            [pulse.util :refer :all]
            [taoensso.timbre :as log]
            )
  (:import (org.bson.types ObjectId)))

;; Simple implementation

; data for this implementation
(def EVENT "event")
(def ENTITY "entity")
(def COUNTERS "counter")

; Database operations
(defonce db (atom nil))

(defn init-db [host port]
  (if (not @db)
    (let [conn (mg/connect {:host host :port port})]
      (reset! db (mg/get-db conn "pulse"))
      (if-not (mc/find-one @db COUNTERS {:name "events"}) (mc/insert @db COUNTERS {:name "events" :value 0})))))

(defn- next-count [name]
  (:value 
   (mc/find-and-modify @db COUNTERS {:name name} {$inc {:value 1}} {:return-new true})))

(defn- store [event]
  {:pre [(:type event)]}
  (mc/save @db EVENT (assoc  event :system {:timestamp (System/currentTimeMillis) :counter (next-count "events")})))

(defn- read-events []
  (mc/find-maps @db EVENT))

(defn- read-event [id]
  (mc/find-one-as-map @db EVENT {:_id (ObjectId. id)}))

(defonce clients (atom []))
; impl
(deftype simple-event-bus []
  pulse/event-bus
  (publish [this event]
    {:pre [(:type event)]}
    (do
      (store event)
      (.play-event this event)
      ))
  (subscribe-for-type [_ handler type]
    {:pre (fn? handler)}
    (swap! clients conj [handler type]))
  (subscribe-for-all [this handler]
    (.subscribe-for-type this handler ::all))
  (unsubscribe-all [_] (reset! clients []))
  (unsubscribe [_ _] (throw (Exception. "not implemented")))
  (get-events [_] (map #(update-in % [:type] keyword) (read-events)))
  (get-event [_ event-id] (update-in (read-event event-id) [:type] keyword))
  (get-clients [_] @clients)
  (play-event [_ e]
    (doseq [[handler type] @clients]
      (if (or (= type (keyword (:type e)))
              (= type ::all))
        (handler e))))
  )


(defn read-entity [id]
  (if-let [entity (mc/find-one-as-map ENTITY {:_id id})]
    (assoc entity :type (keyword (:type entity)))))




(deftype simple-command-bus []
  pulse/command-bus
  (pass-command [_ id cmd] (let [entity (read-entity id)
                                 _ (log/debug "Loaded entity with id[ " id "]: " entity)
                                 events (read-events id)
                                 entity (reduce #(pulse/process-event %1 %2) entity events)]
                             (pulse/process-command entity cmd)))
  (new-entity [_ type payload]
    (let [entity (mc/save-and-return ENTITY (assoc payload :_id (uuid) :type type))]
      (:_id entity)))

  )

(deftype logclient []
  pulse/client
  (notify [_ event] (log/debug "NOTIFIED: " event)))
(deftype printclient []
  pulse/client
  (notify [_ event] (println "NOTIFIED: " event)))

(defn create-pulse-simple [host port]
  (init-db host port)
  {
   :command-bus (simple-command-bus.)
   :event-bus   (simple-event-bus.)
   })

(defn set-simple! [host port]
  (pulse/set-pulse! (create-pulse-simple host port)))

(defn update-events-type []
  "temp fix"
  (mc/update @db EVENT {} {$set {:type :fetched-qpx}} {:multi true}))

(defn get-latest-events[n]
  (let [types (mc/distinct @db EVENT "type")]
    (mapcat (fn [t]  
              (q/with-collection @db EVENT
                (q/find {:type t})
                (q/sort (array-map :timestamp -1))
                (q/limit n)))
            types)
    )
)







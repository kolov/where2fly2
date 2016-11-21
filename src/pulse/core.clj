(ns pulse.core
  (:require
    [pulse.util :refer :all]
    ))

(defonce the-pulse (atom nil))

(defn set-pulse! [pulse]
  "sets a pulse implementation as default by altering *pulse* var"
  (reset! the-pulse pulse))

(defprotocol event-bus
  (publish [this event])
  (subscribe-for-type [this handler type])
  (subscribe-for-all [this handler])
  (unsubscribe [this client])
  (unsubscribe-all [this])
  (get-events [this])
  (get-event [this event-id])
  (play-event [this event])
  (get-clients [this])
  )

(defprotocol client
  (notify [this event]))

; Command
(defprotocol command-bus
  (new-entity [this type value])
  (pass-command [this id cmd])
  )

(defmulti process-event
          (fn [entity event] [(:type entity) (:type event)]))

(defmulti process-command
          (fn [entity cmd]
            (println "Process-command " cmd " on " entity)
            [(:type entity) (:type cmd)]))


;; Global Shortcuts


(defn pass-command [id cmd]
  {:pre [@the-pulse]}
  (.pass-command (:command-bus @the-pulse) id cmd))

(defn new-entity [type payload]
  {:pre [@the-pulse type]}
  (.new-entity (:command-bus @the-pulse) type payload))

(defn publish [evt]
  {:pre [@the-pulse]}
  (.publish (:event-bus @the-pulse) evt))


(defn subscribe-for-type [handler type]
  {:pre [the-pulse]}
  (.subscribe-for-type (:event-bus @the-pulse) handler type))

(defn unsubscribe-all []
  {:pre [the-pulse]}
  (.unsubscribe-all (:event-bus @the-pulse)))

(defn subscribe-for-all [handler]
  {:pre [@the-pulse]}
  (.subscribe-for-all (:event-bus @the-pulse) handler))

(defn replay-events
  ([ebus]
   (doseq [e (.get-events ebus)] (.play-event ebus e)))
  ([]
   {:pre [@the-pulse]}
   (replay-events (:event-bus @the-pulse))))

(defn replay-event
  ([ebus id]
   {:pre [@the-pulse]}
   (.play-event ebus (.get-event ebus id)))
  ([id]
   {:pre [@the-pulse]}
   (replay-event (:event-bus @the-pulse) id)))


(defn get-events []
  {:pre [@the-pulse]}
  (.get-events (:event-bus @the-pulse)))



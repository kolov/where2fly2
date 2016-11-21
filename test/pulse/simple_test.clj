(ns pulse.simple-test
  (:require
    [clojure.test :refer :all]
    [clojure.pprint :refer :all]
    [midje.sweet :refer :all]
    [monger.core :as mg]
    [pulse.simple :as s]
    [pulse.core :as pc]
    [monger.collection :as mc])
  )

(def event-bus (s/->simple-event-bus))

(defn reset-test-db []
  (if (not @s/db)
    (let [conn (mg/connect {:host "localhost" :port 27017})]
      (reset! s/db (mg/get-db conn "pulse-test"))
      (println "Connected to test database " @s/db)))
  (mc/remove @s/db s/EVENT)
  )


(defonce counter-all (atom 0))
(defonce counter-type2 (atom 0))

(def event-bus (s/->simple-event-bus))

(defn client1 [_] (swap! counter-all inc))
(defn client2 [_] (swap! counter-type2 inc))



(with-state-changes
  [(before :facts (do (reset-test-db)
                      (reset! counter-all 0)
                      (reset! counter-type2 0)
                      (.unsubscribe-all event-bus)
                      (.subscribe-for-all event-bus client1)
                      (.subscribe-for-type event-bus client2 :type2)
                      ))]
  (facts "About listeners"

         @counter-all => 0
         (fact "events empty at begin"
               (count (.get-events event-bus)) => 0
               )

         (fact "only listener of the right type called 1"
               @counter-all => 0
               @counter-type2 => 0
               (.publish event-bus {:type :test})
               @counter-all => 1
               @counter-type2 => 0
               (count (.get-events event-bus)) => 1
               )


         (fact "only listener of the right type called 2"
               @counter-all => 0
               @counter-type2 => 0
               (.publish event-bus {:type :type2})
               (count (.get-events event-bus)) => 1
               @counter-all => 1
               @counter-type2 => 1
               )

         (fact "replay works "
               (.publish event-bus {:type :test})
               (.publish event-bus {:type :type2})
               @counter-all => 2
               @counter-type2 => 1
               (count (.get-events event-bus)) => 2

               (pc/replay-events event-bus)
               @counter-all => 4
               @counter-type2 => 2

               (count (.get-events event-bus)) => 2
               )
         ))

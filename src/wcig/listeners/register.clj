(ns wcig.listeners.register
  (:require
    [pulse.core :as p]
    [wcig.listeners.q :as q]
    [wcig.listeners.transavia :as transavia]
    ))


(defn register-listeners []
  (p/unsubscribe-all)
  (p/subscribe-for-type transavia/update-flightinfo-listener :fetched-transavia-flight-offers)
  (p/subscribe-for-type transavia/update-routes-listener :fetched-transavia-routes)
  (p/subscribe-for-type q/qpx-trip-options-listener :fetched-qpx-trip-options)
  )

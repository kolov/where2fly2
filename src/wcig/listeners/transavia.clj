(ns wcig.listeners.transavia
  (:require
    [slingshot.slingshot :refer [throw+]]
    [wcig.util :refer :all]
    [monger.operators :refer :all]
    [wcig.dbbase :refer :all]
    [wcig.db :as db]
    [wcig.core :as core]
    [taoensso.timbre :as log]))

(defn flight-duration [origin departureDateTime destination arrivalDateTime]
  (flight-duration-with-tz departureDateTime arrivalDateTime (db/airports-time-dif origin destination)))

(defn update-routes-listener [evt]
  (println "update-routes-listener called.")
  (let [ts (now)
        all-routes (:routes evt)]
    (if (seq? (seq all-routes))
      (do
        (log/info "event contains " (count all-routes) " valid routes")
        (doseq [origin core/transavia-origins]
          (db/save-transavia-routes-from-origin
            {:origin       origin
             :ts           ts
             :destinations (->> all-routes
                                (filter #(= (get-in %
                                                    [:originDestinationInformation
                                                     :originLocation
                                                     :locationCode]) origin))
                                (map #(get-in %
                                              [:originDestinationInformation
                                               :destinationLocation
                                               :locationCode])))
             })))
      (log/info "event contains no valid routes")
      )))



(defn get-departure-date [f]
  (unparse-std-date (parse-iso-datetime (get-in f [:outboundFlight :departureDateTime]))))


(defn update-flightinfo-listener [evt]
  "Saves flights-by-date"
  (let [flights (get-in evt [:response :flightOffer])
        flights-by-date (group-by get-departure-date flights)
        records (for [[date flights-on-date] flights-by-date]
                  (let [origin (get-in (first flights-on-date) [:outboundFlight :departureAirport :locationCode])
                        destination (get-in (first flights-on-date) [:outboundFlight :arrivalAirport :locationCode])]
                    {
                     :departureDate date
                     :origin        origin
                     :destination   destination
                     :group         :transavia

                     :flights       (vec (map (fn [fl] (let [
                                                             fl (assoc fl :departureDateTime
                                                                          (parse-iso-datetime (get-in fl
                                                                                                      [:outboundFlight
                                                                                                       :departureDateTime])))
                                                             fl (assoc fl :arrivalDateTime
                                                                          (parse-iso-datetime (get-in fl
                                                                                                      [:outboundFlight
                                                                                                       :arrivalDateTime])))

                                                             fl (assoc fl :price (get-in fl [:pricingInfoSum
                                                                                             :totalPriceAllPassengers]))
                                                             fl (assoc fl :flightNumber (get-in fl [:outboundFlight
                                                                                                    :flightNumber]))
                                                             fl (assoc fl :carrier (get-in fl [:outboundFlight
                                                                                               :marketingAirline
                                                                                               :companyShortName]))
                                                             fl (dissoc fl :outboundFlight :pricingInfoSum :deeplink)
                                                             fl (assoc fl :duration (flight-duration origin
                                                                                                     (:departureDateTime fl)
                                                                                                     destination
                                                                                                     (:arrivalDateTime fl)))
                                                             ]
                                                         fl))
                                              flights-on-date
                                              ))
                     })
                  )
        ]
    (doseq [r records] (db/save-flights-on-date r))))



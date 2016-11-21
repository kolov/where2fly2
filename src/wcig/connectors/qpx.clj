(ns wcig.connectors.qpx
  (:require [org.httpkit.client :as client]
            [confiture.core :refer [value]]
            [clojure.data.json :as json]
            [slingshot.slingshot :refer [throw+]]
            [wcig.util :refer :all]
            [wcig.connectors.base :refer :all]

            ) )


(def QPX-URL "https://www.googleapis.com/qpxExpress/v1/trips/search")
(def FIELDS "kind,trips(data(aircraft,carrier),tripOption(saleTotal,slice,pricing(saleFareTotal,saleTotal,saleTaxTotal,baseFareTotal)))")

(defn find-async [^String origin ^String o-date ^String dest ^String i-date]
  (client/post
    (str QPX-URL "?key=" (value :google-geocoding-key) "&fields=" FIELDS)
    {:headers {"Content-Type" "application/json"}
     :body
              (json/write-str
                {
                 :request {
                           :slice       [{
                                          :kind        "qpxexpress#sliceInput"
                                          :origin      origin,
                                          :destination dest,
                                          :date        o-date
                                          :maxStops    1}
                                         {:kind        "qpxexpress#sliceInput"
                                          :origin      dest
                                          :destination origin
                                          :date        i-date
                                          :maxStops    1}
                                         ]
                           :passengers  {
                                         :kind              "qpxexpress#passengerCounts",
                                         :adultCount        1,
                                         :infantInLapCount  0,
                                         :infantInSeatCount 0,
                                         :childCount        0,
                                         :seniorCount       0
                                         },
                           :solutions   40,
                           :refundable  false
                           :saleCountry "NL"
                           }
                 })} parse-response))
(defn find-sync [^String origin ^String o-date ^String dest ^String i-date]
  @(find-async origin o-date dest i-date))


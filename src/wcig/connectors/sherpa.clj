(ns wcig.connectors.sherpa
  (:require [org.httpkit.client :as client]
            [confiture.core :refer [value]]
            [slingshot.slingshot :refer [throw+]]
            [wcig.connectors.base :refer :all]
            ))

(def SHERPA-BASE-URL (value :sherpa-url))

(def R 6371)
(defn deg2rad [d] (/ (* d Math/PI) 180.0))
(defn rad2deg [d] (/ (* d 180.0) Math/PI))



(defn rect [lat-deg lng-deg r-km]
  "Calculates a rectangle of lat/long [latmin latmax lngmin lngmax"
  (let
    [lat (deg2rad lat-deg)
     lng (deg2rad lng-deg)
     r (/ r-km R)
     dlon (Math/asin (/ (Math/sin r) (Math/cos lat)))
     dlat (Math/sin r)
     ]
    [
     (rad2deg (- lat dlat))
     (rad2deg (+ lat dlat))
     (rad2deg (- lng dlon))
     (rad2deg (+ lng dlon))
     ]

    ))

(defn get-pics
  [lat lng r min-date max-date]
  (let [ url (str SHERPA-BASE-URL
                 "/poi?lat=" lat
                 "&lon=" lng
                 "&radius=" r
                 "&minDate=" (subs min-date 0 (- (count min-date) 3))
                 "&maxDate=" (subs max-date 0 (- (count max-date) 3))
                 )]
    (println "URL: " url)
    @(client/get
       url
       {}
       parse-response)))

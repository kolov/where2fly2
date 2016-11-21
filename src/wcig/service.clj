(ns wcig.service
  (:require
    [clojure.data.json :as json]
    [wcig.util :refer :all]
    [wcig.db :as db]
    [slingshot.slingshot :refer [throw+]]
    [wcig.data :as data]
    [wcig.view :as view]
    [wcig.search :as search]
    [wcig.connectors.sherpa :as sherpa]
    [wcig.ref.init :as refinit]
    [jota.core :as log]
    [clojure.edn :as edn])
  (:import (org.joda.time LocalDate DateTime)
           (java.util Date)
           (java.util.regex Pattern)))

(defn writer-as-date [_ value]
  (cond
    (= (class value) LocalDate) (unparse-std-local-date value)
    :default value))

(defn writer-as-local-date [_ value]
  (cond
    (= (class value) LocalDate) (unparse-std-local-date value)
    (= (class value) DateTime) (unparse-std-local-date (.toLocalDate value))
    :default value))

(defn write-json-as-date [val]
  (json/write-str val :value-fn writer-as-date))

(defn write-json-as-local-date [val]
  (json/write-str val :value-fn writer-as-local-date))

(defn respond-as-json [data]
  {:status  200
   :headers {"Content-type" "application/json"}
   :body    (write-json-as-local-date data)})


(defn origin [code]
  "get information about airport code"
  (let [airport (db/find-airport (.toUpperCase code))
        airport (dissoc airport :_id)]
    (respond-as-json airport)))



(defn destinations [scope origins]
  (let [
        dests (cond
                (= scope "holiday") (concat data/holiday-airports)
                (= scope "transavia") (db/get-transavia-destnations origins)
                :default (throw+ (str "Unknown group " scope))
                )
        dests (map #(if-let [found (db/find-airports %)]
                     found
                     (log/error (str "Airport not found " %)))
                   dests)
        dests (remove nil? dests)
        dests (flatten dests)
        dests (map #(dissoc % :_id :tz :city-code :country-code) dests)]
    dests))


(defn rename-key [col from to] (dissoc (assoc col to (from col)) from))

(defn modify-time [t] (-> t (rename-key :local-as-utc :lutc) (dissoc :utc)))

(defn return-with-cache [result]
  {:body    (write-json-as-date result)
   :status  200
   :headers {"Content-type"  "application/json"
             "Cache-Control" "public, max-age=3600"
             }}
  )

(defn itineraries-json [origin ^String out-date ^String in-date params]
  (return-with-cache (view/get-itineraries-from origin out-date in-date params)))

(defn slices-json [origin ^String out-date destination ^String in-date group]
  (return-with-cache (view/get-slices origin out-date destination in-date group)))

(defn flights-from-json [origin ^String out-date params]
  (return-with-cache (view/get-flights-from origin out-date params)))

(defn flights-to-json [origin ^String in-date params]
  (return-with-cache (view/get-flights-to origin in-date params)))



(defn available-weekends-from [origins groups]
  (respond-as-json (search/coming-weekends origins)))

(defn available-holidays-from [origins groups]
  (respond-as-json (search/coming-holidays groups)))

(defn log-access-event [req]
  (let [evt {:action      :access
             :ts          (Date.)
             :uri         (:uri req)
             :headers     (:headers req)
             :remote-addr (:remote-addr req)}]
    (log/info evt)
    (db/save-event evt))
  )


(defn log-user-event [req]
  (log/debug "LOGGING")
  (let [evt (assoc (:body req)
              :ts (Date.)
              :remote-addr (:remote-addr req))]
    (log/info evt)
    (db/save-event evt))
  )

(defn get-user-events []
  (write-json-as-date (db/get-user-events)))

(def popular-codes ["BCN" "VLC"])

(defn get-pics [code min-date max-date]
  (if-let [airport (refinit/find-airport code)]
    (let [loc (or (:location (:city airport)) (:location airport))
          pics (sherpa/get-pics (:lat loc) (:lng loc) 12 min-date max-date)
          place (or (get-in airport [:city :name]) code)
          ;; pics (if (seq pics) pics (sherpa/get-pics (:lat loc) (:lng loc) 12 1418860 1418860000))
          ]
      (println "pics" pics)
      (take 5 (map #(assoc % :place place) pics))
      )))

(defn inspiration [code min-date max-date]
  {:body "[]"})
;(let [places (if code [code] popular-codes)
;      resp (flatten (for [code places] (get-pics code min-date max-date)))
;      ]
;  {:body (json/write-str resp)}))

(def config-transavia
  {
   :showZoomButtons           false
   :origins                   [
                               {:code "AMS" :active true}
                               {:code "RTM" :active false}
                               {:code "GRQ" :active false}
                               {:code "EIN" :active false}
                               ]
   :groups                    [:transavia]
   :periods                   [:weekend :holiday]
   :holiday-map-center        [43, 0]
   :holiday-map-zoom          4
   :holiday-duration-min      2
   :holiday-duration-max      36
   :holiday-duration          20
   :holiday-price-min         200
   :holiday-price-max         3000
   :holiday-price             2500
   :holiday-out-range         [0, 24]
   :holiday-in-range          [0, 24]
   :holiday-price-color-low   250
   :holiday-price-color-high  550
   :holiday-out-departure-min -48
   :holiday-out-departure-max 72
   :holiday-in-departure-min  -48
   :holiday-in-departure-max  72
   :holiday-around            "2d"


   :weekend-map-center        [43, 0]
   :weekend-map-zoom          4
   :weekend-duration-min      1
   :weekend-duration-max      12
   :weekend-duration          5
   :weekend-price-min         60
   :weekend-price-max         1200
   :weekend-price             400
   :weekend-out-range         [0, 24]
   :weekend-in-range          [0, 24]
   :weekend-price-color-low   80
   :weekend-price-color-high  450
   :weekend-out-departure-min -48
   :weekend-out-departure-max 72
   :weekend-in-departure-min  -48
   :weekend-in-departure-max  72
   :weekend-around            "2d"
   })

(def config-holidays
  {
   :showZoomButtons           true
   :origins                   [{:code "AMS" :active true}]
   :groups                    [:holiday]
   :periods                   [:holiday]
   :holiday-map-center        [20, 0]
   :holiday-map-zoom          2
   :holiday-duration-min      2
   :holiday-duration-max      36
   :holiday-duration          18
   :holiday-price-min         200
   :holiday-price-max         3000
   :holiday-price             2500
   :holiday-out-range         [0, 24]
   :holiday-in-range          [-24, 24]
   :holiday-price-color-low   450
   :holiday-price-color-high  1250
   :holiday-out-departure-min -24
   :holiday-out-departure-max 72
   :holiday-in-departure-min  -48
   :holiday-in-departure-max  72
   :holiday-around            "2d"


   :weekend-map-center        [43, 0]
   :weekend-map-zoom          4
   :weekend-duration-min      1
   :weekend-duration-max      12
   :weekend-duration          5
   :weekend-price-min         60
   :weekend-price-max         1200
   :weekend-price             400
   :weekend-out-range         [0, 24]
   :weekend-in-range          [0, 24]
   :weekend-price-color-low   80
   :weekend-price-color-high  450
   :weekend-out-departure-min -48
   :weekend-out-departure-max 72
   :weekend-in-departure-min  -48
   :weekend-in-departure-max  72
   :weekend-around            "2d"
   })

(def configurations
  {
   "transavia" config-transavia
   "holidays"  config-holidays
   })

(def app-config
  (let [r (clojure.java.io/resource "wcig.edn")]
    (println "Resource: " r)
    (delay (edn/read-string (slurp r)))))

(defn cfg-from-server-name [server-name]
  (println "searching " server-name " in " app-config)
  (get-in @app-config [:configurations server-name]))

(defn get-configuration [cfg server-name]
  "Returns configuration pased on 'cfg' parameter in request and server-name"
  (let [cfg-name (if cfg cfg (cfg-from-server-name server-name))
        result (get configurations cfg-name)
        result (if result result (do (log/info "Cannot determine configuration, using default") config-transavia))
        result (assoc result :applications (get-in @app-config [:applications server-name]))]
    result))


(defn is-mobile-browser? [req]
  (if-let [agent (get-in req [:headers "user-agent"])]
    (let [agent (.toLowerCase agent)]
      (or
        (re-matches (Pattern/compile (str "(?i).*((android|bb\\\\d+|meego)"
                                          ".+mobile|avantgo|bada\\\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)"
                                          "|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)"
                                          "\\\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\\\.(browser|link)|vodafone|wap|windows ce|xda|xiino).*"))
                    agent)
        (re-matches (Pattern/compile (str "(?i)1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\\\-)|ai(ko|rn)"
                                          "|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\\\-m"
                                          "|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\\\-(n|u)|c55\\\\/|capi|ccwa|"
                                          "cdm\\\\-|cell|chtm|cldc|cmd\\\\-|co(mp|nd)|craw|da(it|ll|ng)|"
                                          "dbte|dc\\\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\\\-d)|el(49|ai)|em(l2|ul)|"
                                          "er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\\\-|_)|g1 u|g560|gene|"
                                          "gf\\\\-5|g\\\\-mo|go(\\\\.w|od)|gr(ad|un)|haie|hcit|hd\\\\-(m|p|t)|hei\\\\-|"
                                          "hi(pt|ta)|hp( i|ip)|hs\\\\-c|ht(c(\\\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\\\-(20|go|ma)|i230|iac( |\\\\-|\\\\/)|ibro|"
                                          "idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\\\/)|klon|kpt |"
                                          "kwc\\\\-|kyo(c|k)|le(no|xi)|lg( g|\\\\/(k|l|u)|50|54|\\\\-[a-w])|libw|lynx|m1\\\\-w|m3ga|m50\\\\/|ma(te|ui|xo)"
                                          "|mc(01|21|ca)|m\\\\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|"
                                          "n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|"
                                          "owg1|p800|pan(a|d|t)|pdxg|pg(13|\\\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\\\-2|po(ck|rt|se)|prox|psio|"
                                          "pt\\\\-g|qa\\\\-a|qc(07|12|21|32|60|\\\\-[2-7]|i\\\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|"
                                          "s55\\\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\\\-|oo|p\\\\-)|sdk\\\\/|se(c(\\\\-|0|1)"
                                          "|47|mc|nd|ri)|sgh\\\\-|shar|sie(\\\\-|m)|sk\\\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|"
                                          "h\\\\-|v\\\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\\\-|tdg\\\\-|"
                                          "tel(i|m)|tim\\\\-|t\\\\-mo|to(pl|sh)|ts(70|m\\\\-|m3|m5)|tx\\\\-9|up(\\\\.b|g1|si)|utst|v400"
                                          "|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)"
                                          "|w3c(\\\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\\\\-|your|zeto|zte\\\\-"))
                    (subs agent 0 4))))))



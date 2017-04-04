(ns wcig.web
  (:require [compojure.route :as r]
            [compojure.core :refer (GET POST defroutes)]
            [compojure.handler :as handler]
            ;[com.akolov.mirador.core :refer :all]


            [cornet.core :as cc]
            [cornet.route :as cr]

            [ring.util.response :as resp]
            [ring.middleware.session]
            [ring.middleware.cookies]
            [ring.middleware.reload]
            [com.akolov.enlive-reload]
            [ring.middleware.not-modified]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-body]]

            [confiture.core :refer [value]]
            [taoensso.timbre :as log]

            [org.httpkit.server :refer [run-server]]

            [wcig.ref.init :as init]
            [wcig.service :as svc]
            [wcig.dbmemo :as dbmemo]
            [wcig.util :refer :all]
            [wcig.ref.init :refer :all]
            [wcig.listeners.register :as lr]
            [wcig.templates :as tmpl]
            [pulse.simple :as simple]
            [pulse.core :as p]
            [wcig.dbbase :as dbbase]
            [wcig.core :as core]
            [wcig.jobs :refer [start-jobs]]
            )
  )


(defonce appmode (atom nil))

(def response-ok {:status 200 :body "OK"})



(defroutes routes

           (GET "/loaderio-e7f18d2d79fb6a6157e537f35b07bec8/" req {:body "loaderio-e7f18d2d79fb6a6157e537f35b07bec8"
                                                                   :status 200})

           (GET "/" req (if (svc/is-mobile-browser? req)
                          (tmpl/mobile-template)
                          (tmpl/main req @appmode)))
           (GET "/m" req (tmpl/main req @appmode))
           (GET "/v" req (slurp (clojure.java.io/resource "buildtime")))
           (GET "/disclaimer" req (tmpl/disclaimer req @appmode))
           (GET "/about" req (tmpl/about req @appmode))
           (GET "/stats" req (tmpl/stats req @appmode))

           (GET "/v1/init/db" [] (do (init/update-airports-missing-tz)
                                     (core/update-transavia-routes)
                                     (core/update-transavia-flightsinfo)
                                     response-ok)
                                 )
           (GET "/v1/update/transavia" _ (do (core/update-transavia-flightsinfo) response-ok))
           (GET "/v1/inspiration" [code minDate maxDate] (svc/inspiration code minDate maxDate))
           (GET "/v1/destinations" [groups origins]
             (svc/respond-as-json (svc/destinations groups (parse-cs-strings origins))))
           (GET "/v1/config" [cfg :as req]
             (svc/respond-as-json (svc/get-configuration cfg (:server-name req))))
           (GET "/v1/airport/:code" [code] (svc/origin code))
           (GET "/v1/itineraries/origin/:origin/group/:group/out/:out-date/in/:in-date"
                [origin out-date in-date around group]
             (svc/itineraries-json (.toUpperCase origin) out-date in-date
                                   {:around around :group group}))
           (GET "/v1/slices/origin/:origin/destination/:destination/group/:group/out/:out-date/in/:in-date"
                [origin destination out-date in-date group]
             (svc/slices-json (.toUpperCase origin) out-date destination in-date
                              group))
           (GET "/v1/flights/from/:origin/group/:group/date/:out-date"
                [origin out-date :as req]
             (svc/flights-from-json (.toUpperCase origin) out-date
                                    (update-in (:params req) [:dests] parse-cs-strings)))
           (GET "/v1/flights/to/:origin/group/:group/date/:in-date"
                [origin in-date :as req]
             (svc/flights-to-json (.toUpperCase origin) in-date
                                  (update-in (:params req) [:dests] parse-cs-strings)))
           ; dates
           (GET "/v1/weekends" [origins groups] (svc/available-weekends-from origins groups))
           (GET "/v1/holidays" [origins groups] (svc/available-holidays-from origins groups))
           (GET "/v1/invalidate-all" req (do (dbmemo/invalidate-all-calls) response-ok))
           (GET "/v1/invalidate/:name" req (do (dbmemo/invalidate-call name) response-ok))
           (POST "/v1/evt" req (do (svc/log-user-event req) response-ok))
           (GET "/v1/events" req (svc/get-user-events))

           (GET "/browser" req {:status 200
                                :body   (str
                                          "user-agent: [" (get-in req [:headers "user-agent"]) "]"
                                          "is-mobile: [" (svc/is-mobile-browser? req) "]"
                                          )})

           (cr/wrap-url-response (cc/static-assets-loader "/s" :mode :prod))
           (r/resources "/static" {:root "public"})
           (r/resources "/bower" {:root "components"})
           (r/not-found (resp/file-response "not-found.html" {:root "resources/private"}))

           )

(defn check-browser [handler]
  (fn [req] (let [bs (get-in req [:headers "user-agent"])]
              (log/debug "user-agent:" bs)
              (if (or (.contains bs "Chrome")
                      (.contains bs "Firefox")
                      (.contains bs "safari")
                      (.contains bs "curl/"))
                (handler req)
                {:status 200 :body
                         (str "Sorry, this site has only been tested with chrome, firefox and safari browser
                                ."
                              bs)}))))

(def app-develop (-> #'routes
                     (ring.middleware.reload/wrap-reload)
                     (com.akolov.enlive-reload/wrap-enlive-reload)
                     (ring.middleware.params/wrap-params)
                     (wrap-json-body)
                     (ring.middleware.not-modified/wrap-not-modified)
                     ;(watch-reload {:watcher (watcher-folder "resources") :uri "/watch-reload"})
                     handler/site))

(def app-prod (-> routes
                  ;(logger/wrap-with-logger
                  ;  :info (fn [x] (log/info x))
                  ;  :debug (fn [x] (log/debug x))
                  ;  :error (fn [x] (log/error x))
                  ;  :warn (fn [x] (log/warn x))
                  ;  )
                  (wrap-json-body)
                  (ring.middleware.not-modified/wrap-not-modified)
                  (ring.middleware.params/wrap-params)
                  handler/site))



(defonce stop-fn (atom nil))

(defn init-app
  ([host port]
   ":init in ring config"
   (log/info "Buildtime " (slurp (clojure.java.io/resource "buildtime")))
   (dbbase/init host port)
   (log/debug "db initialized: " @dbbase/db)
   (p/set-pulse! (simple/create-pulse-simple host port))
   (lr/register-listeners)
   (start-jobs)
   (update-airports-missing-tz))
  ([] (init-app "localhost" 27017))
  )


(defn- start-with-fn [f]
  (when @stop-fn (println "Stopping runing server") (@stop-fn))
  (reset! stop-fn (f))
  (wcig.dbmemo/invalidate-call "itineraries")
  (println "Started server, stop with (stop-server)"))

(defn define-app-mode []
  "Retreives app mode from config"
  (let [
        app-env (value :app-env)
        app-env (if (seq app-env) app-env "PROD")
        app-env (-> app-env .toLowerCase keyword)]
    app-env
    ))

(defn start-server [mode port dbhost dbport]
  "Start the right configm depending on app-env"
  (println "Starting application in mode [" mode "]")
  (reset! appmode mode)
  (init-app (str dbhost) dbport)
  (let [
        params {:port port :join? false}
        run-fn (mode {:prod #(run-server app-prod params)
                      :dev  #(run-server #'app-develop params)})
        _ (if (nil? run-fn) (throw (Exception. (str "Can't start with profile" mode))))]
    (start-with-fn run-fn)))


(defn start-dev []
  "start in dev mode during development ONLY"
  (start-server :dev 3000 "localhost" 27017))
(defn start-prod []
  "atart in prod mode during development ONLY"
  (start-server :prod 3000 "localhost" 27017))


(defn stop-server [] (@stop-fn) (reset! stop-fn nil))



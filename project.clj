(defproject wherecanigo "0.1.0-SNAPSHOT"
  :description "The smart flight selector"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring "1.3.2"]
                 [http-kit "2.1.18"]
                 [org.clojure/data.json "0.2.6"]
                 [com.novemberain/monger "2.0.0"]
                 [clj-http "2.2.0"]
                 [com.akolov/mirador "0.2.2-SNAPSHOT"]
                 [compojure "1.3.3"]
                 [jota "0.7.7-SNAPSHOT"]
                 [slingshot "0.12.2"]
                 [clj-time "0.9.0"]
                 [ring.middleware.logger "0.5.0" :exclusions [log4j]]
                 [ring/ring-json "0.4.0" :exclusions [ring/ring-core cheshire org.clojure/tools.reader]]
                 [cornet "0.1.0"]
                 [enlive "1.1.5"]
                 [clojurewerkz/quartzite "2.0.0"]
                 [com.akolov.enlive-reload "0.2.1"]
                 [confiture "0.1.1"]
                 ]
  :plugins [
            [lein-resource "15.10.1"]
            ]
  :main wcig.run
  :resource-paths ["resources" "bower" "target/stencild"]
  :prep-tasks ["javac" "compile" "resource"]
  :repositories
  ^:replace
  [["mirror-akolov" "http://nexus.akolov.com/content/repositories/central/"]
   ["snapshots" "http://nexus.akolov.com/content/repositories/snapshots/"]
   ["clojar-mirror" "https://clojars.org/repo"]             ;://nexus.akolov.com/content/repositories/clojars-mirror/"]
   ["releases" "http://nexus.akolov.com/content/repositories/releases/"]]
  :plugin-repositories ^:replace [["clojar-mirror" "https://clojars.org/repo"]] ;://nexus.akolov.com/content/repositories/clojars-mirror/"]]

  :bower-dependencies [
                       [bootstrap "3.0.0"]
                       [angular-ui-codemirror "0.1.7"]
                       [bootstrap-social "latest"]
                       [angular "1.2.19"]
                       [angular-route "1.2.19"]
                       [angular-resource "1.2.19"]
                       [angular-ui-router "0.2.13"]
                       [angular-bootstrap "0.12.1"]
                       [angular-sanitize "1.2.19"]
                       [angular-ui-switch "0.1.0"]
                       [angular-load "0.2.0"]
                       [underscore "1.8.3"]
                       [angular-google-maps "2.1.5"]
                       [seiyria-bootstrap-slider "4.10.1"]
                       [ngmap "1.7.11"]
                       [angular-click-outside "2.1.0"]
                       [jquery ">= 1.9"]
                       [jquery-ui "~1.10.x"]
                       [moment "2.10.6"]
                       [angular-smart-table "2.1.3"]
                       ]
  :bower {:directory "bower/components"}
  :resource {;; used by resource plugin

             :resource-paths ["src/to_stencil"]             ;; required or does nothing
             :target-path    "target/stencild"              ;; optional default to the global one
             :update         false                          ;; if true only process files with src newer than dest

             }
  :cljsbuild {
              :builds [{
                        ; The path to the top-level ClojureScript source directory:
                        :source-paths ["src-cljs"]
                        ; The standard ClojureScript compiler options:
                        ; (See the ClojureScript compiler documentation for details.)
                        :compiler     {
                                       :output-to     "war/js/main.js" ; default: target/cljsbuild-main.js
                                       :optimizations :advanced
                                       :pretty-print  true}}]}
  :profiles {:dev  {:dependencies
                                    [[midje "1.6.3"]
                                     [midje-junit-formatter "0.1.0-SNAPSHOT"]]
                    :plugins        [
                                     [lein-environ "1.0.0"]
                                     [lein-midje "3.0.0"]
                                     [lein-bower "0.5.1"]
                                     ]
                    :resource-paths ["config/dev"]
                    :env
                                    {
                                     :sherpa-url "http://localhost:9000"
                                     }

                    }
             :prod {
                    :resource-paths ["config/prod"]
                    }
             })

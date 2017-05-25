(defproject wherecanigo "0.1.0-SNAPSHOT"
  :description "The smart flight selector"
  :url "https://github.com/kolov/where2fly2"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring "1.3.2"]
                 [http-kit "2.1.18"]
                 [org.clojure/data.json "0.2.6"]
                 [com.novemberain/monger "2.0.0"]
                 [clj-http "2.2.0"]
                 [compojure "1.3.3"]
                 [jarohen/chime "0.2.1"]
                 [com.taoensso/timbre "4.8.0"]
                 [log4j/log4j "1.2.17"]
                 [slingshot "0.12.2"]
                 [clj-time "0.9.0"]
                 [ring.middleware.logger "0.5.0" :exclusions [log4j]]
                 [ring/ring-json "0.4.0" :exclusions [ring/ring-core cheshire org.clojure/tools.reader]]
                 [cornet "0.1.0"]
                 [confiture "0.1.1"]
                 ]
  :plugins [
            [lein-resource "15.10.1"]
            ]
  :main wcig.run
  :resource-paths ["resources" "bower" "target/stencild"]
  :prep-tasks ["javac" "compile" "resource"]

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
                                     [lein-bower "0.5.2"]
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

(ns wcig.run

  (:require
    [wcig.web :as web]
    )
  (:gen-class)
  )


(defn -main [& args] (apply web/start-server (map read-string args)))

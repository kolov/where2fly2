(ns wcig.templates

  (:use [net.cgrand.enlive-html]
        [net.cgrand.reload]
        [wcig.util]))


(defmacro deftemplate- [name source & rest]
  `(deftemplate ~name (clojure.java.io/resource (str "pages/" ~source)) ~@rest))
(defmacro defsnippet- [name source selector args & forms]
  `(defsnippet ~name (clojure.java.io/resource (str "pages/" ~source)) ~selector ~args ~@forms))


(defsnippet- reload-script "snippets.html" [:div#reload-script] [])
(defsnippet- ga-script "snippets.html" [:div#ga-script] [])


(defsnippet- index-content "main.html"
             [:div#wcig-app] []
             )

(defsnippet- disclaimer-content "disclaimer.html"
             [:div#disclaimer] []
             )
(defsnippet- about-content "about.html"
             [:div#about] []
             )
(defsnippet- stats-content "stats.html"
             [:div#stats] []
               )

(deftemplate- main-template "template-main.html" [req main mode]
              [:div#main-content] main
              [:div#ga-script] (substitute (ga-script))
              [:div#reload-script] (if (= :dev mode) (substitute (reload-script)))
               )
(deftemplate- mobile-template "mobile.html" [] )

(defn main [req mode]
  (main-template req (substitute (index-content)) mode))
(defn disclaimer [req mode]
  (main-template req (substitute (disclaimer-content)) mode))
(defn about [req mode]
  (main-template req (substitute (about-content)) mode))

(defn stats [req mode]
  (main-template req (substitute (stats-content)) mode))


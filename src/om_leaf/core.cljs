(ns ^:figwheel-always om-leaf.core
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.core.async :refer [<! put!]]
              [cljs-http.client :as http]
              [sablono.core :refer-macros [html]]
              [om.core :as om :include-macros true]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world! We're making queries!"}))

(def overpass-api-url "http://www.overpass-api.de/api/xapi")
(defn overpass-node-query [key value bboxstring]
  (str overpass-api-url "?node[" key "=" value "][bbox=" bboxstring "]"))
(def neighbourhood-query
  (overpass-node-query "place" "neighbourhood"
                                "85.2677,27.64859,85.37207,27.76166"))

(defn query-result-view
  [data owner]
  (om/component
    (html [:p (:text data)])))

(go
 (let [url neighbourhood-query
       result (:body (<! (http/get url {:with-credentials? false})))]
   (reset! app-state {:text (str result)})))

(om/root query-result-view
         app-state
         {:target (. js/document (getElementById "app"))})



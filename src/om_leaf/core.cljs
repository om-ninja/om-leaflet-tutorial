(ns ^:figwheel-always om-leaf.core
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.core.async :refer [<! put!]]
              [cljs-http.client :as http]
              [sablono.core :refer-macros [html]]
              [om.core :as om :include-macros true]
              [cljsjs.jquery]
              [osmtogeojson]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Querying..."}))

(def overpass-api-url "http://www.overpass-api.de/api/xapi")
(defn overpass-node-query [key value bboxstring]
  (str overpass-api-url "?node[" key "=" value "][bbox=" bboxstring "]"))
(def neighbourhood-query
  (overpass-node-query "place" "neighbourhood"
                                "85.2677,27.64859,85.37207,27.76166"))

(defn osm-xml->geojson
  [osm-xml-string]
  "Takes OSM XML in string form, and returns cljs geojson."
  (js->clj (js/osmtogeojson (.parseXML js/jQuery osm-xml-string))
           :keywordize-keys true))

(defn query-result-view
  [data owner]
  (om/component
    (html [:p (:text data)])))

(when (= (:text @app-state "Querying..."))
  (go
   (let [url neighbourhood-query
         result (:body (<! (http/get url {:with-credentials? false})))]
     (reset! app-state {:text (prn-str (osm-xml->geojson result))}))))

(om/root query-result-view
         app-state
         {:target (. js/document (getElementById "app"))})

(ns puppetlabs.bidi-utils
  (:require [bidi.ring :as bidi-ring]
            [clojure.zip :as zip]))

(defn update-route-info
  [route-info pattern]
  (cond
    (contains? #{:get :post :put :delete :head} pattern)
    (assoc-in route-info [:method] pattern)

    :else
    (update-in route-info [:path] concat (flatten [pattern]))))

(declare route-metadata*)

(defn nested-route-metadata*
  [routes route-info loc]
  (let [[pattern matched] (zip/node loc)]
    (cond
      (map? matched)
      (route-metadata*
        routes
        route-info
        (zip/vector-zip [pattern (into [] matched)]))

      (vector? matched)
      (route-metadata*
        routes
        (update-route-info route-info pattern)
        (-> loc zip/down zip/right zip/down))

      :else
      (conj routes (update-route-info route-info pattern)))))

(defn route-metadata*
  [routes route-info loc]
  (loop [routes routes
         loc    loc]
    (let [routes (nested-route-metadata* routes route-info loc)]
      (if-let [next (zip/right loc)]
        (recur routes next)
        routes))))

(defn route-metadata
  [routes]
  (let [route-info {:path   []
                    :method :any}
        loc        (-> [routes] zip/vector-zip zip/down)]
    (route-metadata* [] route-info loc)))






(defn routes->handler
  [routes]
  (with-meta
    (bidi-ring/make-handler routes)
    {:routes (route-metadata routes)}))

(defn context [url-prefix routes]
  [url-prefix [routes]])

(def context-handler (comp routes->handler context))

(defn routes
  [& routes]
  ["" (into [] routes)])

(defn GET
  [pattern handler]
  [pattern {:get handler}])
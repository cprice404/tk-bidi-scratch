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
      (do
        (route-metadata*
          routes
          route-info
          (zip/vector-zip [pattern (into [] matched)])))

      (vector? matched)
      (do
        (route-metadata*
          routes
          (update-route-info route-info pattern)
          (-> loc zip/down zip/right zip/down)))

      :else
      (do
        (conj routes (update-route-info route-info pattern))))))

(defn route-metadata*
  [routes route-info loc]
  (println "LOC:" (zip/node loc))
  (loop [routes routes
         loc loc]
    (let [routes (nested-route-metadata* routes route-info loc)]
      (if-let [next (zip/right loc)]
        (recur routes next)
        routes)))

  #_(if (nil? loc)
    routes
    (cond
      (vector? (zip/node loc))
      (let [routes (nested-route-metadata* routes route-info loc)]
        (let [next (zip/right loc)]
          (route-metadata*
            routes
            route-info
            next)))

      :else
      (throw (IllegalStateException. "d'oh")))))

(defn route-metadata
  [routes]
  (->> [routes]
       zip/vector-zip
       zip/down
       (route-metadata* []
                        {:path []
                         :method :any})))






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
(ns puppetlabs.bidi-utils
  (:require [bidi.ring :as bidi-ring]
            [clojure.string :as str]
            [clojure.zip :as zip]))

#_(defn maps-to-vectors*
  [l]
  (cond
    (zip/end? l)
    (-> l zip/root)

    (map? (zip/node l))
    (maps-to-vectors* (zip/edit l #(into [] %)))

    :else
    (maps-to-vectors* (zip/next l))))

#_(defn maps-to-vectors
  [routes]
  (-> routes zip/vector-zip maps-to-vectors*))

(defn update-route-info
  [route-info pattern]
  (cond
    (contains? #{:get :post :put :delete :head} pattern)
    (assoc-in route-info [:method] pattern)

    :else
    (update-in route-info [:path] concat
               (if (vector? pattern)
                 pattern
                 [pattern]))))

(defn route-metadata*
  [routes route-info loc]
  (if (nil? loc)
    routes
    (let [node (zip/node loc)]
      (cond
        (map? node)
        (do
          (route-metadata* routes route-info (zip/edit loc #(into [] %))))

        (vector? node)
        (let [[pattern matched] (zip/node loc)]
          (let [routes (cond
                         (map? matched)
                         (do
                           (route-metadata* routes route-info
                                            (zip/vector-zip [pattern (into [] matched)])))

                         (vector? matched)
                         (do
                           (route-metadata*
                             routes
                             (update-route-info route-info pattern)
                             (-> loc zip/down zip/right zip/down)))

                         :else
                         (do
                           (conj routes (update-route-info route-info pattern))))]
            (let [next (zip/right loc)]
              (route-metadata*
                routes
                route-info
                next))))

        :else
        (throw (IllegalStateException. "d'oh"))))))

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
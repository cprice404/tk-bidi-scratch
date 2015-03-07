(ns puppetlabs.bidi-utils
  (:require [bidi.ring :as bidi-ring]
            [clojure.string :as str]
            [clojure.zip :as zip]))

(defn maps-to-vectors*
  [l]
  (cond
    (zip/end? l)
    (-> l zip/root)

    (map? (zip/node l))
    (maps-to-vectors* (zip/edit l #(into [] %)))

    :else
    (maps-to-vectors* (zip/next l))))

(defn maps-to-vectors
  [routes]
  (-> routes zip/vector-zip maps-to-vectors*))

(defn update-route-info
  [route-info pattern]
  (cond
    (contains? #{:get :post :put :delete :head} pattern)
    (assoc-in route-info [:method] (-> pattern name str/upper-case))

    :else
    (update-in route-info [:path] str
               (if (vector? pattern)
                 (str/join pattern)
                 pattern))))

(defn print-routes
  ([routepair]
   (print-routes {:path "" :method :any} routepair))
  ([route-info [pattern matched]]
   (let [route-info (update-route-info route-info pattern)]
     (if (vector? matched)
       (doseq [routepair matched]
         (print-routes route-info routepair))
       (println (str (:method route-info) ": " (:path route-info)))))))



(defn context [url-prefix routes]
  (let [context-routes (maps-to-vectors [url-prefix [routes]])]
    (with-meta
      (bidi-ring/make-handler
        context-routes)
      {:routes context-routes})))

(defn routes
  [& routes]
  ["" (into [] routes)])

(defn GET
  [pattern handler]
  [pattern {:get handler}])
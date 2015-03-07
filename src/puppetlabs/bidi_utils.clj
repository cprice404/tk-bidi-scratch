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

#_(defn update-route-info
  [route-info pattern]
  (cond
    (contains? #{:get :post :put :delete :head} pattern)
    (assoc-in route-info [:method] (-> pattern name str/upper-case))

    :else
    (update-in route-info [:path] str
               (if (vector? pattern)
                 (str/join pattern)
                 pattern))))

#_(defn print-routes
  ([routepair]
   (print-routes {:path "" :method :any} routepair))
  ([route-info [pattern matched]]
   (let [route-info (update-route-info route-info pattern)]
     (if (vector? matched)
       (doseq [routepair matched]
         (print-routes route-info routepair))
       (println (str (:method route-info) ": " (:path route-info)))))))

#_(defn route-metadata*
  [acc ])

#_(defn nested-routes*
  [routes route-info pattern matched]
  )

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
  [depth routes route-info loc]
  (if (nil? loc)
    routes
    (let [node (zip/node loc)]
      (println depth "LOC node:" node)
      (cond
        (map? node)
        (do
          (println depth "FOUND MAP AT ROOT LEVEL")
          (route-metadata* (inc depth) routes route-info (zip/edit loc #(into [] %))))

        (vector? node)
        (let [[pattern matched] (zip/node loc)]
          (println depth "PATTERN:" pattern)
          (let [routes (cond
                         (map? matched)
                         (do
                           (println depth "Found a map for MATCHED, converting")
                           (route-metadata* (inc depth)
                                            routes route-info
                                            #_(-> loc
                                                zip/down
                                                zip/right
                                                (zip/edit #(into [] %))
                                                zip/up)
                                            (zip/vector-zip [pattern (into [] matched)])))

                         (vector? matched)
                         (do
                           (println depth "MATCHED VECTOR:" matched)
                           (println depth "\tDOWN:" (-> loc zip/down zip/right zip/down zip/node))
                           (route-metadata*
                             (inc depth)
                             routes
                             (update-route-info route-info pattern)
                             (-> loc zip/down zip/right zip/down)))

                         :else
                         (do
                           (println depth "\nADDING ROUTE:"
                                    pattern "|"
                                    matched "|"
                                    (update-route-info route-info pattern))
                           (conj routes (update-route-info route-info pattern))
                           #_(route-metadata* routes route-info (zip/next loc))))]
            (println depth "Routes is now:" routes)
            (let [next (zip/right loc)]
              (println depth "Moving to next node:")
              (if (nil? next)
                (println depth "nil")
                (println depth (zip/node next)))
              (route-metadata*
                (inc depth)
                routes
                route-info
                next))))

        :else
        #_(route-metadata* routes route-info (zip/next loc))
        #_(let [[pattern matched] (zip/node loc)]
          (println "PATTERN:" pattern)
          (if (vector? matched)
            (do
              (println "MATCHED VECTOR:" matched)
              (println "\tDOWN:" (-> loc zip/down zip/next zip/node))
              (route-metadata* routes route-info (zip/next loc)))
            (do
              (println "MATCHED IS NOT A VECTOR:" matched)
              (route-metadata* routes route-info (zip/next loc)))))
        (throw (IllegalStateException. "d'oh"))
        ))))

(defn route-metadata
  [routes]
  (->> [routes]
       zip/vector-zip
       zip/down
       (route-metadata* 0
                        []
                        {:path []
                         :method :any})))






(defn routes->handler
  [routes]
  #_(let [vector-routes (maps-to-vectors routes)]
    (with-meta
      (bidi-ring/make-handler
        vector-routes)
      {:routes (route-metadata vector-routes)}))
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
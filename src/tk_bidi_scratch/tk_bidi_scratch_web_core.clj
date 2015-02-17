(ns tk-bidi-scratch.tk-bidi-scratch-web-core
  (:require [tk-bidi-scratch.tk-bidi-scratch-service :as hello-svc]
            [clojure.tools.logging :as log]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [clojure.zip :as zip]
            [clojure.string :as str]))

(defn compojure-app
  [hello-service]
  (compojure/routes
    (compojure/GET "/foo/:caller" [caller]
      (fn [req]
        (log/info "Handling FOO request for caller:" caller)
        {:status  200
         :headers {"Content-Type" "text/plain"}
         :body    (hello-svc/hello hello-service (str "foo" caller))}))
    (compojure/GET "/bar/:caller" [caller]
      (fn [req]
        (log/info "Handling BAR request for caller:" caller)
        {:status  200
         :headers {"Content-Type" "text/plain"}
         :body    (hello-svc/hello hello-service (str "bar" caller))}))
    (route/not-found "Not Found")))

(defn bidi-routes
  [hello-service]
  ["/" {["foo/" :caller]
        [[:get
          (fn [{{:keys [caller]} :route-params}]
            (log/info "Handling FOO request for caller:" caller)
            {:status  200
             :headers {"Content-Type" "text/plain"}
             :body    (hello-svc/hello hello-service (str "foo" caller))})]]

        ["bar/" :caller]
        {:get
         (fn [{{:keys [caller]} :route-params}]
           (log/info "Handling BAR request for caller:" caller)
           {:status  200
            :headers {"Content-Type" "text/plain"}
            :body    (hello-svc/hello hello-service (str "bar" caller))})}}])

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
        (println (str (:method route-info) ": " (:path route-info)) "\n")))))

#_(defn zippy-routes
  [routes]
  (->> (clojure.zip/vector-zip routes)
       (iterate zip/next)
       (take-while (complement zip/end?))
       (filter (comp fn? zip/node))
       (mapv zip/path)
       first
       (mapv zip/node)))

(def r ["/howdy" [(bidi-routes "bunk")]])
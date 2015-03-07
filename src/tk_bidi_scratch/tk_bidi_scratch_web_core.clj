(ns tk-bidi-scratch.tk-bidi-scratch-web-core
  (:require [tk-bidi-scratch.tk-bidi-scratch-service :as hello-svc]
            [clojure.tools.logging :as log]
    ;            [compojure.core :as compojure]
    ;           [compojure.route :as route]
;            [clojure.zip :as zip]
;            [clojure.string :as str]
            [puppetlabs.bidi-utils :as bidi-utils]))


#_(defn bidi-routes
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

#_(defn compojure-app
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
  (bidi-utils/routes
    (bidi-utils/GET ["/foo/" :caller]
           (fn [{{:keys [caller]} :route-params}]
             (log/info "Handling FOO request for caller:" caller)
             {:status  200
              :headers {"Content-Type" "text/plain"}
              :body    (hello-svc/hello hello-service (str "foo" caller))}))
    (bidi-utils/GET ["/bar/" :caller]
           (fn [{{:keys [caller]} :route-params}]
             (log/info "Handling BAR request for caller:" caller)
             {:status  200
              :headers {"Content-Type" "text/plain"}
              :body    (hello-svc/hello hello-service (str "bar" caller))}))))



#_(defn maps-to-vectors
  [l]
  (loop [x l]
    (cond
      (zip/end? x)
      (-> x zip/root)

      (map? (zip/node x))
      (maps-to-vectors (zip/edit x #(into [] %)))

      :else
      (maps-to-vectors (zip/next l)))))



#_(defn zippy-routes
  [routes]
  (->> (clojure.zip/vector-zip routes)
       (iterate zip/next)
       (take-while (complement zip/end?))
       (filter (comp fn? zip/node))
       (mapv zip/path)
       first
       (mapv zip/node)))

#_(def r ["/howdy" [(bidi-routes "bunk")]])

;; (print-routes (-> r zip/vector-zip maps-to-vectors))
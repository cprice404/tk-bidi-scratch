(ns tk-bidi-scratch.tk-bidi-scratch-web-core
  (:require [tk-bidi-scratch.tk-bidi-scratch-service :as hello-svc]
            [clojure.tools.logging :as log]
            [compojure.core :as compojure]
            [compojure.route :as route]))

(defn app
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

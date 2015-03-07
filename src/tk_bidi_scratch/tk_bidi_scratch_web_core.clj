(ns tk-bidi-scratch.tk-bidi-scratch-web-core
  (:require [tk-bidi-scratch.tk-bidi-scratch-service :as hello-svc]
            [clojure.tools.logging :as log]
            [puppetlabs.bidi-utils :as bidi-utils]))

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
(ns tk-bidi-scratch.tk-bidi-scratch-web-service
  (:require [clojure.tools.logging :as log]
            [clojure.pprint :as pprint]
            [tk-bidi-scratch.tk-bidi-scratch-web-core :as core]
            [puppetlabs.trapperkeeper.core :as trapperkeeper]
            [puppetlabs.trapperkeeper.services :as tk-services]
            [puppetlabs.bidi-utils :as bidi-utils]))

(trapperkeeper/defservice hello-web-service
  [[:ConfigService get-in-config]
   [:WebroutingService add-ring-handler get-route]
   HelloService]
  (init [this context]
        (log/info "Initializing hello webservice")
        (let [url-prefix (get-route this)
              app         (bidi-utils/context
                            url-prefix
                            (core/bidi-routes
                              (tk-services/get-service this :HelloService)))]
          (log/info "Adding routes:")
          (bidi-utils/print-routes (:routes (meta app)))
          (add-ring-handler this app)
          (assoc context :url-prefix url-prefix)))

  (start [this context]
         (let [host (get-in-config [:webserver :host])
               port (get-in-config [:webserver :port])
               url-prefix (get-route this)]
              (log/infof "Hello web service started; visit http://%s:%s%s/(foo|bar)/world to check it out!"
                         host port url-prefix))
         context))

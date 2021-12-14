(ns whether.main
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [whether.constants :as const]
            [whether.routes :as routes]
            [whether.cron :as whether-cron]
            [whether.utils :refer [ignore-trailing-slash]])
  (:gen-class))

(def app
  (-> routes/app-routes
      (ignore-trailing-slash)
      (wrap-json-body)
      (wrap-json-response)
      (wrap-defaults (assoc site-defaults
                            :static {:resources "public"}
                            :security {:anti-forgery false}
                            :params {:keywordize true
                                     :urlencoded true}))
      (wrap-cors :access-control-allow-origin const/cor-origins
                 :access-control-allow-methods [:get :post])))

(defn -main [& args]
  (if (seq args)
    (case (first args)
      "--cron-forecasts" (whether-cron/update-nea-forecasts)
      "--cron-readings" (whether-cron/update-nea-realtime-readings))
    (do
      (println "Whether Report in "
               (if const/dev? "development" "production")
               " mode running on port" const/port)
      (run-server app {:port const/port}))))

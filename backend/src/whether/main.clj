(ns whether.main
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [whether.constants :as const]
            [whether.routes :as routes]
            [whether.cron :as whether-cron]
            [whether.utils :refer [ignore-trailing-slash]]
            [whether.time :as t])
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
      "--cron-30" (apply whether-cron/update-nea-db-for-period
                         (t/get-last-30-days))
      "--cron-2" (apply whether-cron/update-nea-db-for-period
                        (t/get-last-n-days 2))
      "--cron-x" (whether-cron/update-nea-db-for-period (nth args 1) (nth args 2)))
    (do
      (println "Whether Report in "
               (if const/dev? "development" "production")
               " mode running on port" const/port)
      (run-server app {:port const/port}))))

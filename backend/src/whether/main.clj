(ns whether.main
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [whether.constants :as const]
            [whether.routes :as routes]
            [whether.cron :as whether-cron]
            [whether.utils :refer [ignore-trailing-slash]]
            [whether.time :as t]
            [whether.views :refer [generate-static-assets]]
            [whether.db :as db])
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
                                     :urlencoded true}))))

(defn -main [& args]
  (if (seq args)
    (case (first args)
      "--setup-db" (db/setup-db)
      "--cron-30" (apply whether-cron/update-nea-db-for-period
                         (t/get-last-30-days))
      "--cron-2" (apply whether-cron/update-nea-db-for-period
                        (t/get-last-n-days 2))
      "--cron-last-x" (apply whether-cron/update-nea-db-for-period
                             (t/get-last-n-days (Integer. (second args))))
      "--cron-x" (whether-cron/update-nea-db-for-period (nth args 1) (nth args 2))
      "--build" (generate-static-assets))
    (do
      (println "Whether Report in"
               (if const/dev? "development" "production")
               "mode running on port" const/port)
      (run-server app {:port const/port}))))

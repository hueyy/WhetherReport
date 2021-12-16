(ns whether.routes
  (:require [compojure.core :refer [defroutes GET context]]
            [compojure.route :as route]
            [whether.db :as db]
            [whether.log :as l]
            [whether.time :as t]
            [whether.utils :refer [json-response return-400
                                   return-404]]
            [whether.views :as v]))

(def nea-types {:rainfall "rainfall"
                :temperature "temperature"})

(defn get-nea-mistakes
  ([type]
   (l/log "type" type)
   (cond
     (or (nil? type)
         (= type (:rainfall nea-types))) (->> (db/select-nea-rainfall-mistakes)
                                              (json-response))
     :else (return-400 "Invalid type"))))

(defn get-nea-rainfall-accuracy [from to]
  (l/log "from" from "to" to)
  (if (or (t/is-after? to from) (t/shorter-than-2h? from to)
          (t/in-future? from) (t/in-future? to))
    (let [time-periods (t/get-2h-range from to)
          mistakes (db/select-nea-rainfall-mistakes time-periods)]
      (if (nil? mistakes)
        (return-404 "No data found")
        (json-response {:accuracy (- 1 (/ (->> (map #(:timestamp %) mistakes)
                                               (set)
                                               (count))
                                          (count time-periods)))
                        :mistakes mistakes})))
    (return-400 "Invalid time range")))

(defroutes app-routes
  (GET "/" [] (v/index-page))
  (route/resources "/")
  ;; (context "/api" []
  ;;   (GET "/" [] (json-response {:status "OK"}))
  ;;   (GET "/mistakes" [type] (get-nea-mistakes type))
  ;;   (GET "/rainfall-accuracy" [from to] (get-nea-rainfall-accuracy from to)))
  (route/not-found "Not Found"))
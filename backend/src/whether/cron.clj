(ns whether.cron
  (:require [whether.nea :as nea]
            [whether.db :as db]
            [whether.utils :refer [average]]
            [whether.time :as t]
            [whether.log :as l])
  (:gen-class))

(defn aggregate-rainfall-by-station-id [rainfall-readings station-id]
  (let [readings (->> rainfall-readings
                      (map (fn [reading] (->> reading
                                              :items
                                              first
                                              :readings
                                              (filter #(= (:station_id %) station-id))
                                              first
                                              :value))))]
    (if (every? some? readings) ; handle station migrations
      (reduce #(if (nil? %2) %1 (+ %1 %2)) readings)
      nil)))

(defn get-rainfall-by-area [rainfall-readings area]
  (let [station-ids (get nea/forecast-rainfall-stations area)]
    (if (vector? station-ids)
      (try
        (->> station-ids
             (map #(aggregate-rainfall-by-station-id rainfall-readings %))
             (filter some?) ; handle station migrations
             (average))
        (catch Exception e (do (l/debug (->> station-ids
                                             (map #(aggregate-rainfall-by-station-id rainfall-readings %))
                                             (filter #(not (nil? %)))))
                               (l/debug station-ids rainfall-readings area)
                               (l/error e)
                               (throw (Exception. (.getMessage e))))))
      (aggregate-rainfall-by-station-id rainfall-readings station-ids))))

(defn get-rainfall-for-forecast [timestamp]
  (->> (t/add-2h timestamp)
       (t/get-5-min-range timestamp)
       (db/select-nea-rainfall-readings)))

(defn check-for-nea-rainfall-mistakes
  ([] (check-for-nea-rainfall-mistakes (t/get-last-half-hour)))
  ([timestamp]
   (l/trace "checking for NEA rainfall mistakes " timestamp)
   (let [forecast (:raw_data (db/select-nea-weather-forecasts timestamp))
         rainfall-readings (map #(:raw_data %) (get-rainfall-for-forecast timestamp))]
     (if (nil? forecast)
       (l/trace "no forecast for" timestamp)
       (doseq [area-forecast (nea/get-area-forecasts-from-2h-forecast forecast)]
         (if (not (nil? area-forecast))
           (let [area (:area area-forecast)
                 rainfall-value (get-rainfall-by-area rainfall-readings area)
                 forecast-text (:forecast area-forecast)]
             (if (nil? rainfall-value)
               (l/trace "Missing station reading for" area)
               (do
                 (l/trace area rainfall-value)
                 (if (nea/within-expected-rainfall-for-forecast? forecast-text rainfall-value)
                   nil
                   (db/insert-nea-rainfall-mistakes {:area area
                                                     :forecast forecast-text
                                                     :timestamp timestamp
                                                     :actual_rainfall rainfall-value})))))
           (l/trace "NEA returned invalid data, ignoring")))))))

(defn check-for-nea-mistakes
  ([timestamp] (check-for-nea-rainfall-mistakes timestamp)))

(defn generate-nea-forecast-data
  "populates nea_weather_forecasts_data based on raw data from nea_weather_forecasts"
  [])

(defn generate-nea-rainfall-data
  "populates nea_rainfall_readings_data based on raw data from nea_rainfall_readings"
  [])

(defn update-nea-rainfall-readings
  ([] (update-nea-rainfall-readings (t/get-last-half-hour)))
  ([timestamp] (-> (db/select-nea-rainfall-readings timestamp)
                   :raw_data
                   nil?
                   (if (-> (nea/get-rainfall timestamp)
                           (db/insert-nea-rainfall-readings))
                     (l/trace "Rainfall readings already exist")))))

(defn update-nea-temperature-readings
  ([] (update-nea-temperature-readings (t/get-last-half-hour)))
  ([timestamp] (-> (db/select-nea-temperature-readings timestamp)
                   :raw_data
                   nil?
                   (if (-> (nea/get-temperature timestamp)
                           (db/insert-nea-temperature-readings))
                     (l/trace "Temperature readings already exist")))))

(defn update-nea-forecasts
  ([timestamp]
   (let [in-db? (-> (db/select-nea-weather-forecasts timestamp)
                    :raw_data
                    nil?)]
     (if in-db?
       (let [result (nea/get-2h-forecast timestamp)]
         (if (nil? result)
           (db/insert-nea-weather-forecasts
            {:items [{:valid_period {:start timestamp
                                     :end (t/add-2h timestamp)}}]})
           (db/insert-nea-weather-forecasts result)))
       (l/trace "Forecasts already exist")))))

; note the 10 min buffer time
(defn update-nea-realtime-readings
  ([timestamp]
   (update-nea-temperature-readings timestamp)
   (update-nea-rainfall-readings timestamp)))

(defn update-nea-realtime-readings-for-period [from-date to-date]
  (doall (pmap (fn [timestamp]
                 (l/debug "updating NEA realtime readings for timestamp" timestamp)
                 (update-nea-realtime-readings timestamp))
               (t/get-5-min-range from-date to-date))))

(defn update-nea-db-for-period [from-date to-date]
  ; to be run only for historical periods
  (l/debug "updating NEA DB for period" from-date to-date)
  (l/debug "updating NEA forecasts")
  (doall (pmap
          (fn [timestamp]
            (l/debug "updating NEA forecasts for timestamp" timestamp)
            (update-nea-forecasts timestamp))
          (t/get-30-min-range from-date to-date)))
  (l/debug "updating NEA realtime readings")
  (update-nea-realtime-readings-for-period from-date to-date)
  (l/debug "checking for NEA mistakes")
  (doseq [timestamp (t/get-30-min-range from-date to-date)]
    (check-for-nea-mistakes timestamp)))

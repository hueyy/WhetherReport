(ns whether.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [whether.utils :refer [make-json from-json]]
            [whether.log :as l]
            [whether.constants :as const]
            [clojure.string :as str]))

(defn sql-query-log [sym sql-params]
  (prn sym sql-params)
  (System/currentTimeMillis))

(defn sql-result-log [sym state result]
  (prn sym
       (- (System/currentTimeMillis) state)
       (if (map? result) result (count result))))

(def db (-> {:dbtype "sqlite"
             :dbname const/db-file}
            (jdbc/with-options {:return-keys true
                                :builder-fn rs/as-unqualified-maps})
            ;; (jdbc/with-logging
            ;;   sql-query-log
            ;;   sql-result-log)
            ))

(defn setup-db []
  (with-open [connection (jdbc/get-connection db)]
    ; data from NEA API
    (jdbc/execute! connection ["CREATE TABLE IF NOT EXISTS `nea_weather_forecasts` (`raw_data` TEXT NOT NULL, `valid_from` TEXT PRIMARY KEY NOT NULL, `valid_to` TEXT NOT NULL)"])
    (jdbc/execute! connection ["CREATE TABLE IF NOT EXISTS `nea_temperature_readings` (`raw_data` TEXT NOT NULL, `timestamp` TEXT PRIMARY KEY NOT NULL)"])
    (jdbc/execute! connection ["CREATE TABLE IF NOT EXISTS `nea_rainfall_readings` (`raw_data` TEXT NOT NULL, `timestamp` TEXT PRIMARY KEY NOT NULL)"])
    ; generated data
    (jdbc/execute! connection ["CREATE TABLE IF NOT EXISTS `nea_rainfall_mistakes` (`area` TEXT NOT NULL, `timestamp` TEXT NOT NULL, `forecast` TEXT NOT NULL, `actual_rainfall` INTEGER NOT NULL, PRIMARY KEY (`area`, `timestamp`))"])))

(def default-opts {:return-keys true
                   :builder-fn rs/as-unqualified-maps})

(defn parse-raw-data [{raw-data :raw_data :as result}]
  (assoc result :raw_data (from-json raw-data)))

(defn select-nea-weather-forecasts
  ([] (->> (sql/query db ["SELECT * FROM `nea_weather_forecasts` WHERE 1 ORDER BY `valid_from`"])
           (map parse-raw-data)))
  ([valid-from] (try
                  (-> (sql/get-by-id db :nea_weather_forecasts valid-from :valid_from {})
                      (parse-raw-data))
                  (catch Exception e (l/error e)))))

(defn select-nea-temperature-readings
  ([] (->> (sql/query db ["SELECT * FROM `nea_temperature_readings` WHERE 1 ORDER BY `timestamp`"])
           (map parse-raw-data)))
  ([timestamp] (try
                 (-> (sql/get-by-id db :nea_temperature_readings timestamp :timestamp {})
                     (parse-raw-data))
                 (catch Exception e (l/error e)))))

(defn select-nea-rainfall-readings
  ([] (->> (sql/query db ["SELECT * FROM `nea_rainfall_readings` WHERE 1 ORDER BY `timestamp`"])
           (map parse-raw-data)))
  ([timestamp] (if (seq? timestamp)
                 (try
                   (let [sql-array (str/join "," (-> (count timestamp)
                                                     (repeat "?")))
                         query (into [(str "SELECT * FROM `nea_rainfall_readings` WHERE `timestamp` IN (" sql-array ") ORDER BY `timestamp`")]
                                     timestamp)]
                     (->> (sql/query db query)
                          (map parse-raw-data)))
                   (catch Exception e (l/error e)))
                 (try
                   (-> (sql/get-by-id db :nea_rainfall_readings timestamp :timestamp {})
                       (parse-raw-data))
                   (catch Exception e (l/error e))))))

(defn insert-nea-weather-forecasts [raw-data]
  (let [{:keys [start end]} (:valid_period (first (:items raw-data)))
        raw-data-text (make-json raw-data)]
    (l/trace "Inserting NEA weather forecast start=" start)
    (try
      (sql/insert! db :nea_weather_forecasts {:raw_data raw-data-text
                                              :valid_from start
                                              :valid_to end})
      (catch Exception e (l/error e)))))

(defn insert-nea-temperature-readings [raw-data]
  (let [timestamp (:timestamp (first (:items raw-data)))
        raw-data-text (make-json raw-data)]
    (l/trace "Inserting NEA temperature reading timestamp=" timestamp)
    (try
      (sql/insert! db :nea_temperature_readings {:raw_data raw-data-text
                                                 :timestamp timestamp})
      (catch Exception e (l/error e)))))

(defn insert-nea-rainfall-readings [raw-data]
  (let [timestamp (:timestamp (first (:items raw-data)))
        raw-data-text (make-json raw-data)]
    (l/trace "Inserting NEA rainfall reading timestamp=" timestamp)
    (try
      (sql/insert! db :nea_rainfall_readings {:raw_data raw-data-text
                                              :timestamp timestamp})
      (catch Exception e (l/error e)))))

(defn insert-nea-rainfall-mistakes [{area :area
                                     timestamp :timestamp
                                     forecast :forecast
                                     actual_rainfall :actual_rainfall
                                     :as input}]
  (l/trace "Inserting NEA rainfall mistakes" input)
  (try
    (sql/insert! db :nea_rainfall_mistakes {:area area
                                            :timestamp timestamp
                                            :forecast forecast
                                            :actual_rainfall (double actual_rainfall)})
    (catch Exception e (l/error e))))

(defn select-nea-rainfall-mistakes
  ([] (l/trace "Selecting NEA rainfall mistakes")
      (try
        (sql/query db ["SELECT * FROM `nea_rainfall_mistakes` WHERE 1 ORDER BY `timestamp`"])
        (catch Exception e (l/error e))))
  ([timestamps] (if (seq? timestamps)
                  (try
                    (let [sql-array (str/join "," (repeat (count timestamps) "?"))
                          query (into [(str "SELECT * FROM `nea_rainfall_mistakes` WHERE `timestamp` IN (" sql-array) ") ORDER BY `timestamp`"] timestamps)]
                      (sql/query db query))
                    (catch Exception e (l/error e)))
                  (select-nea-rainfall-mistakes (repeat 1 timestamps)))))

(ns whether.time
  (:require [whether.log :as l])
  (:import [java.time ZonedDateTime]
           [java.time.format DateTimeFormatterBuilder]
           [java.time.temporal ChronoUnit]
           [java.util Locale]
           [java.time.format TextStyle])
  (:gen-class))

(defn format-as-iso8601 [time]
  (-> (new DateTimeFormatterBuilder)
      (.appendPattern "yyyy-MM-dd")
      (.appendLiteral "T")
      (.appendPattern "HH:mm:ss")
      (.appendLiteral "+08:00")
      (.toFormatter)
      (.format time)))

(defn round-time
  "Round time using (round-fn minutes), taking in optional ISO8601
   timestamp as 1st arg"
  ([round-fn] (round-time (ZonedDateTime/now) round-fn))
  ([timestamp round-fn]
   (let [now (if (string? timestamp) (ZonedDateTime/parse timestamp) timestamp)
         minutes (.getMinute now)
         minutesToAdd (round-fn minutes)
         roundedTime (-> now
                         (.truncatedTo ChronoUnit/HOURS)
                         (.plusMinutes minutesToAdd))]
     (format-as-iso8601 roundedTime))))

(defn get-nearest-hour []
  (round-time #(if (< % 30) 0 60)))

(defn get-nearest-half-hour []
  (round-time #(* 30 (+ (quot % 30)
                        (if (> (rem % 30) 15) 1 0)))))

(def last-half-hour-rounding-fn #(if (< % 30) 0 30))
(defn get-last-half-hour
  ([] (round-time last-half-hour-rounding-fn))
  ([timestamp] (round-time timestamp last-half-hour-rounding-fn)))

(def buffered-half-hour-rounding-fn
  #(+ 10 (last-half-hour-rounding-fn %)))
(defn get-buffered-last-half-hour
  ([] (round-time buffered-half-hour-rounding-fn))
  ([timestamp] (round-time timestamp buffered-half-hour-rounding-fn)))

(defn is-after? [date1 date2]
  (.isAfter (ZonedDateTime/parse date1) (ZonedDateTime/parse date2)))

(defn shorter-than-2h? [from to]
  (< (.until (ZonedDateTime/parse from)
             (ZonedDateTime/parse to) (ChronoUnit/HOURS)) 2))

(defn in-future? [date]
  (is-after? date (ZonedDateTime/now)))

(defn get-time-range [num unit from-date to-date]
  (let [from (ZonedDateTime/parse from-date)
        to (ZonedDateTime/parse to-date)]
    (if (.isAfter to from)
      (let [diff (.until from to unit)
            time-seq (range 0 (+ diff 1) num)] ; + 1 to include to in seq
        (map #(->> (.plus from % unit)
                   format-as-iso8601) time-seq))
      (l/error (Exception. "from-date is after to-date")))))

(defn get-min-range [min f t] (get-time-range min ChronoUnit/MINUTES f t))

(defn get-5-min-range [f t] (get-min-range 5 f t))

(defn get-30-min-range [f t] (get-min-range 30 f t))

(defn get-2h-range [f t] (get-time-range 2 ChronoUnit/HOURS f t))

(defn add-2h [timestamp]
  (-> (ZonedDateTime/parse timestamp)
      (.plusHours 2)
      (format-as-iso8601)))

(defn compare-timestamp [a b] (.isBefore (ZonedDateTime/parse a)
                                         (ZonedDateTime/parse  b)))

(defn get-today []
  (let [now (ZonedDateTime/now)]
    (map format-as-iso8601 [(.truncatedTo now ChronoUnit/DAYS)
                            (-> (.truncatedTo now ChronoUnit/HOURS)
                                (.minusHours 2))])))

(defn get-last-n-days [n]
  (let [now (ZonedDateTime/now)]
    (map format-as-iso8601 [(-> (.truncatedTo now ChronoUnit/DAYS)
                                (.minusDays n))
                            (-> (.truncatedTo now ChronoUnit/HOURS)
                                (.minusHours 2))])))

(defn get-last-7-days [] (get-last-n-days 7))

(defn get-last-30-days [] (get-last-n-days 30))

(defn format-as-day [timestamp]
  (let [t (ZonedDateTime/parse timestamp)]
    (str (.getDayOfMonth t) " "
         (-> (.getMonth t)
             (.getDisplayName TextStyle/SHORT Locale/UK)) " "
         (.getYear t))))
(ns whether.log
  (:require [taoensso.timbre :as t]
            [whether.constants :as const]))

(def log-level (if const/dev? :debug :warn))

(t/set-level! log-level)

(defn error [error] (t/error (.getMessage error)))

(defn log [& args] (t/info args))

(defn debug [& args] (t/debug args))

(defn trace [& args] (t/trace args))
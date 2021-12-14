(ns whether.utils
  (:require [clojure.math.numeric-tower :as math]
            [jsonista.core :as j]
            [whether.time :as t]))

(defn make-json [input-data]
  (j/write-value-as-string input-data j/keyword-keys-object-mapper))

(defn from-json [input-text]
  (j/read-value input-text j/keyword-keys-object-mapper))

(defn ignore-trailing-slash
  "Modifies the request uri before calling the handler.
  Removes a single trailing slash from the end of the uri if present.

  Useful for handling optional trailing slashes until Compojure's route matching syntax supports regex.
  Adapted from http://stackoverflow.com/questions/8380468/compojure-regex-for-matching-a-trailing-slash"
  [handler]
  (fn [request]
    (let [uri (:uri request)]
      (handler (assoc request :uri (if (and (not (= "/" uri)) (.endsWith uri "/"))
                                     (subs uri 0 (dec (count uri)))
                                     uri))))))

(defn json-response
  ([hash-map] (json-response hash-map {}))
  ([hash-map options]
   (merge {:status 200
           :headers {"Content-Type" "application/json"}}
          options
          {:body (make-json hash-map)})))

(defn return-400 [msg]
  (json-response {:message msg} {:status 400}))

(defn return-404 [msg]
  (json-response {:message msg} {:status 400}))

(defn average [numbers]
  (if (empty? numbers)
    0
    (/ (reduce + numbers) (count numbers))))

(defn get-distance [c1 c2]
  (math/sqrt (+ (-> (:lat c1)
                    (- (:lat c2))
                    (math/expt 2))
                (-> (:lng c1)
                    (- (:lng c2))
                    (math/expt 2)))))
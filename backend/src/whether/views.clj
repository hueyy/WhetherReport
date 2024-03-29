(ns whether.views
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :as s]
            [hiccup.page :refer [html5]]
            [whether.constants :as const]
            [whether.utils :refer [from-json make-json]]
            [whether.time :as t]
            [whether.db :as db]
            [whether.nea :as nea])
  (:gen-class))

(def index-path "dist/index.html")

(defn copy-prod-assets []
  (sh "npm" "run" "build" :dir "../frontend")
  (sh "cp" "-r" "../frontend/dist" "."))

(def dev-index-scripts
  (list [:script {:type "module"
                  :src (str const/vite-url "/@vite/client")}]
        [:script {:type "module"
                  :src (str const/vite-url "/src/main.tsx")}]))

(def prod-assets
  (-> (slurp const/manifest-path)
      (from-json)))

(def prod-styles
  (->> (seq prod-assets)
       (filter #(->> % last :css vector?))
       (map #(->> % last :css))
       (flatten)
       (map #(vector :link {:rel "stylesheet" :href (str "/" %)}))))

(def prod-scripts
  (conj
   (->> (seq prod-assets)
        (map #(->> % last :file (str "/")))
        (filter #(s/ends-with? % ".js"))
        (map #(vector :script {:type "module" :src %})))
   [:script {:async true :src "//gc.zgo.at/count.js"
             :data-goatcounter "https://whether-report.goatcounter.com/count"}]))

(def fonts (list [:link {:rel "preconnect"
                         :href "https://fonts.googleapis.com"}]
                 [:link {:rel "4preconnect"
                         :href "https://fonts.gstatic.com"}]
                 [:link {:rel "stylesheet"
                         :href "https://fonts.googleapis.com/css2?family=Fira+Sans:wght@400;600;900&display=swap"}]))

(defn add-accuracy-to-region [[name data] forecasts mistakes]
  (let [forecasts-count (count forecasts)
        mistakes-count (->> mistakes (filter #(= (:area %) name)) (count))]
    (->> (/ mistakes-count forecasts-count)
         (- 1)
         (assoc data :accuracy)
         (vector name))))

(defn filter-rain [forecasts true?]
  (->> forecasts
       (map #(->> % :raw_data :items first :forecasts
                  (filter (fn [f] (-> f :forecast nea/forecasts-rain? (= true?))))))
       (apply concat)
       (count)))

(defn group-by-week [forecasts time-field]
  (group-by (fn [forecast]
              (-> forecast time-field t/get-week))
            forecasts))

(defn calculate-weekly-accuracy [forecasts mistakes]
  (let [f-weeks (group-by-week forecasts :valid_from)
        m-weeks (group-by-week mistakes :timestamp)]
    (->> (seq f-weeks)
         (map (fn [[t f]]
                (let [m (get m-weeks t)]
                  (list t (- 1 (/ (count m)
                                  (* (count f) (count nea/forecast-regions))))))))
         (sort-by #(first %)))))

(defn calculate-rainfall-incidence []
  (/ (apply + (->> (db/select-nea-rainfall-readings)
                   (map #(->> % :raw_data :items first :readings))
                   (map #(count (filter (fn [r] (-> r :value (> 0))) %)))))
     (count nea/forecast-regions)
     (count (db/select-nea-rainfall-readings))))

(defn calculate-forecasts-rain-count [forecasts]
  (/ (apply + (->> forecasts
                   (map #(->> % :raw_data :items first :forecasts
                              (filter (fn [f] (-> f :forecast nea/forecasts-rain?)))
                              (count)))))
     (count nea/forecast-regions)
     (count forecasts)))

(defn generate-data []
  (let [forecasts (->> (db/select-nea-weather-forecasts)
                       (filter #(-> % :raw_data nea/is-valid-forecast?)))
        forecasts-timestamps (->> forecasts
                                  (map :valid_from)
                                  (sort t/compare-timestamp))
        mistakes (db/select-nea-rainfall-mistakes)
        mistakes-count (count mistakes)
        forecasts-count (* (count forecasts)
                           (count nea/forecast-regions))
        mistakes-rain-count (->> mistakes ; i.e. forecast rain and no rain occurred
                                 (filter #(-> % :actual_rainfall (= 0)))
                                 (count))
        mistakes-no-rain-count (->> mistakes ; i.e. forecast no rain and rain occurred
                                    (filter #(-> % :actual_rainfall (> 0)))
                                    (count))
        rain-accuracy (- 1 (/ mistakes-rain-count
                              (filter-rain forecasts true)))
        non-rain-accuracy (- 1 (/ mistakes-no-rain-count
                                  (filter-rain forecasts false)))
        weekly-accuracy (calculate-weekly-accuracy forecasts mistakes)]
    (-> {:forecasts_count forecasts-count
         :mistakes_count mistakes-count
         :period (str (-> forecasts-timestamps first t/format-as-day)
                      " – "
                      (-> forecasts-timestamps last t/format-as-day))
         :accuracy {:overall (- 1 (/ mistakes-count forecasts-count))
                    :rain rain-accuracy
                    :non_rain non-rain-accuracy}
         :regions (->> (seq nea/forecast-regions)
                       (map #(add-accuracy-to-region % forecasts mistakes))
                       (reduce #(assoc %1 (first %2) (last %2)) {}))
         :weekly_accuracy weekly-accuracy
         :forecasts_rain_count (calculate-forecasts-rain-count forecasts)
         :rainfall_incidence (calculate-rainfall-incidence)}
        (make-json))))

;TODO: additional accuracies in chart
(defn index-page []
  (html5 {:mode :html}
         [:head
          [:meta {:charset "UTF-8"}]
          [:meta {:name "viewport"
                  :content "width=device-width, initial-scale=1.0"}]
          [:title "WhetherReport"]
          fonts
          prod-styles]
         [:body
          [:div {:id "app"}]
          (if const/dev?
            dev-index-scripts
            prod-scripts)
          [:script {:type "text/javascript"}
           (str "const data = " (generate-data) ";")]]))

(defn generate-static-assets []
  (copy-prod-assets)
  (spit index-path (index-page)))
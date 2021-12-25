(ns whether.views
  (:require [clojure.string :as s]
            [hiccup.core :as h]
            [whether.constants :as const]
            [whether.utils :refer [from-json make-json]]
            [whether.time :as t]
            [whether.db :as db]
            [whether.nea :as nea]))

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
        rain-accuracy (- 1 (/ (->> mistakes ; i.e. forecast rain and no rain occurred
                                   (filter #(-> % :actual_rainfall (= 0)))
                                   (count))
                              (filter-rain forecasts true)))
        non-rain-accuracy (- 1 (/ (->> mistakes ; i.e. forecast no rain and rain occurred
                                       (filter #(-> % :actual_rainfall (> 0)))
                                       (count))
                                  (filter-rain forecasts false)))]
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
                       (reduce #(assoc %1 (first %2) (last %2)) {}))}
        (make-json))))

(defn index-page []
  (h/html {:mode :html}
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
(ns whether.nea
  (:require [org.httpkit.client :as http]
            [whether.log :as l]
            [whether.time :as t]
            [whether.utils :refer [from-json
                                   get-distance]]))

(def forecast-temperature-stations
  {"Ang Mo Kio" "S198" ; Ang Mo Kio Avenue 5
   "Bukit Timah" "S50" ; Clementi Road
   "Hougang" "S43" ; Kim Chuan Road
   "City" "S108" ; Marina Gardens Drive
   "Jalan Bahar" "S44" ; Nanyang Avenue
   "Tengah" "S121" ; Old Choa Chu Kang Road
   "Pulau Ubun" "S106" ; Pulau Ubin
   "Novena" "S111" ; Scotts Road
   "Tuas" "S115" ; Tuas South Avenue 3
   "Changi" "S24" ; Upper Changi Road North
   "Clementi" "S116" ; West Coast Highway
   "Woodlands" "S104" ; Woodlands Avenue 9
   "Sungei Kadut" "S100" ; Woodlands Road
   })

(def rainfall-stations
  {"S218" {:lat 1.36491 :lng 103.75065 :name "S218"}
   "S08" {:lat 1.3701 :lng 103.8271 :name "Upper Thomson Road"}
   "S100" {:lat 1.4172 :lng 103.74855 :name "Woodlands Road"}
   "S104" {:lat 1.44387 :lng 103.78538 :name "Woodlands Avenue 9"}
   "S106" {:lat 1.4168 :lng 103.9673 :name "Pulau Ubin"}
   "S107" {:lat 1.3135 :lng 103.9625 :name "East Coast Parkway"}
   "S108" {:lat 1.2799 :lng 103.8703 :name "Marina Gardens Drive"}
   "S109" {:lat 1.3764 :lng 103.8492 :name "Ang Mo Kio Avenue 5"}
   "S111" {:lat 1.31055 :lng 103.8365 :name "Scotts Road"}
   "S112" {:lat 1.43854 :lng 103.70131 :name "Lim Chu Kang Road"}
   "S113" {:lat 1.30648 :lng 103.9104 :name "Marine Parade Road"}
   "S114" {:lat 1.38 :lng 103.73 :name "Choa Chu Kang Avenue 4"}
   "S115" {:lat 1.29377 :lng 103.61843 :name "Tuas South Avenue 3"}
   "S116" {:lat 1.281 :lng 103.754 :name "West Coast Highway"}
   "S118" {:lat 1.2994 :lng 103.8461 :name "Handy Road"}
   "S119" {:lat 1.30105 :lng 103.8666 :name "Nicoll Highway"}
   "S120" {:lat 1.30874 :lng 103.818 :name "Holland Road"}
   "S121" {:lat 1.37288 :lng 103.72244 :name "Old Choa Chu Kang Road"}
   "S123" {:lat 1.3214 :lng 103.8577 :name "Towner Road"}
   "S201" {:lat 1.32311 :lng 103.76714 :name "S201"}
   "S202" {:lat 1.30968 :lng 103.7578 :name "S202"}
   "S203" {:lat 1.29164 :lng 103.7702 :name "S203"}
   "S204" {:lat 1.40081 :lng 103.88217 :name "S204"}
   "S205" {:lat 1.38829 :lng 103.9116 :name "S205"}
   "S207" {:lat 1.32485 :lng 103.95836 :name "S207"}
   "S208" {:lat 1.3136 :lng 104.00317 :name "S208"}
   "S209" {:lat 1.42111 :lng 103.84472 :name "S209"}
   "S210" {:lat 1.44003 :lng 103.76904 :name "S210"}
   "S211" {:lat 1.42918 :lng 103.75711 :name "S211"}
   "S212" {:lat 1.31835 :lng 103.93574 :name "S212"}
   "S213" {:lat 1.32427 :lng 103.8097 :name "S213"}
   "S214" {:lat 1.29911 :lng 103.88289 :name "S214"}
   "S215" {:lat 1.32785 :lng 103.88899 :name "GEYLANG EAST CENTRAL"}
   "S216" {:lat 1.36019 :lng 103.85335 :name "S216"}
   "S217" {:lat 1.35041 :lng 103.85526 :name "S217"}
   "S219" {:lat 1.37999 :lng 103.87643 :name "S219"}
   "S220" {:lat 1.38666 :lng 103.89797 :name "S220"}
   "S221" {:lat 1.35691 :lng 103.89088 :name "S221"}
   "S222" {:lat 1.28987 :lng 103.82364 :name "S222"}
   "S223" {:lat 1.29984 :lng 103.80264 :name "S223"}
   "S224" {:lat 1.34392 :lng 103.98409 :name "S224"}
   "S226" {:lat 1.27472 :lng 103.80389 :name "S226"}
   "S227" {:lat 1.43944 :lng 103.80389 :name "S227"}
   "S228" {:lat 1.34703 :lng 103.70073 :name "S228"}
   "S229" {:lat 1.35167 :lng 103.72195 :name "S229"}
   "S230" {:lat 1.30167 :lng 103.76444 :name "S230"}
   "S24" {:lat 1.3678 :lng 103.9826 :name "Upper Changi Road North"}
   "S33" {:lat 1.3081 :lng 103.71 :name "Jurong Pier Road"}
   "S35" {:lat 1.3329 :lng 103.7556 :name "Old Toh Tuck Road"}
   "S40" {:lat 1.4044 :lng 103.78962 :name "Mandai Lake Road"}
   "S43" {:lat 1.3399 :lng 103.8878 :name "Kim Chuan Road"}
   "S44" {:lat 1.34583 :lng 103.68166 :name "Nanyang Avenue"}
   "S50" {:lat 1.3337 :lng 103.7768 :name "Clementi Road"}
   "S66" {:lat 1.4387 :lng 103.7363 :name "Kranji Way"}
   "S69" {:lat 1.37 :lng 103.805 :name "Upper Peirce Reservoir Park"}
   "S71" {:lat 1.2923 :lng 103.7815 :name "Kent Ridge Road"}
   "S77" {:lat 1.2937 :lng 103.8125 :name "Alexandra Road"}
   "S78" {:lat 1.30703 :lng 103.89067 :name "Poole Road"}
   "S79" {:lat 1.3004 :lng 103.8372 :name "Somerset Road"}
   "S81" {:lat 1.4029 :lng 103.9092 :name "Punggol Central"}
   "S82" {:lat 1.3247 :lng 103.6351 :name "Tuas West Road"}
   "S84" {:lat 1.3437 :lng 103.9444 :name "Simei Avenue"}
   "S88" {:lat 1.3427 :lng 103.8482 :name "Toa Payoh North"}
   "S89" {:lat 1.31985 :lng 103.66162 :name "Tuas Road"}
   "S90" {:lat 1.3191 :lng 103.8191 :name "Bukit Timah Road"}
   "S900" {:lat 1.41284 :lng 103.86922 :name "Seletar Aerospace View"}
   "S94" {:lat 1.3662 :lng 103.9528 :name "Pasir Ris Street 51"}})

(def forecast-rainfall-stations
  {"Ang Mo Kio" ["S109"]
   "Bedok" "S212"
   "Bishan" ["S216" "S217" "S69" "S08"]
   "Boon Lay" "S33"
   "Bukit Batok" "S218"
   "Bukit Merah" ["S222" "S226"]
   "Bukit Panjang" "S218"
   "Bukit Timah" ["S50" "S213"]
   "Central Water Catchment" ["S69" "S08"]
   "Changi" ["S208" "S224" "S24"]
   "Choa Chu Kang" "S218"
   "City" ["S118" "S108" "S79"]
   "Clementi" ["S201" "S202" "S203" "S230"]
   "Geylang" "S215"
   "Hougang" ["S43" "S221"]
   "Jalan Bahar" "S44"
   "Jurong East" "S35"
   "Jurong Island" ["S116" "S33"]
   "Jurong West" ["S228" "S229"]
   "Kallang" ["S119" "S123"]
   "Lim Chu Kang" ["S66" "S112"]
   "Mandai" ["S227" "S40"]
   "Marine Parade" ["S113" "S78" "S214"]
   "Novena" "S111"
   "Pasir Ris" "S94"
   "Paya Lebar" "S221"
   "Pioneer" "S89"
   "Pulau Tekong" "S24"
   "Pulau Ubin" "S106"
   "Punggol" ["S81" "S205"]
   "Queenstown" ["S77" "S71" "S116"]
   "Seletar" ["S204" "S900"]
   "Sembawang" ["S227" "S104"]
   "Sengkang" ["S219" "S220"]
   "Sentosa" "S226"
   "Serangoon" "S216"
   "Southern Islands" "S226"
   "Sungei Kadut" ["S40" "S211" "S100"]
   "Tampines" ["S207" "S84"]
   "Tanglin" ["S90" "S120" "S223"]
   "Tengah" "S121"
   "Toa Payoh" ["S88"]
   "Tuas" ["S89" "S115" "S82"]
   "Western Islands" "S116"
   "Western Water Catchment" "S112"
   "Woodlands" "S210"
   "Yishun" "S209"})

(def forecast-regions
  {"Ang Mo Kio" {:lat 1.375 :lng 103.839}
   "Bedok" {:lat 1.321 :lng 103.924}
   "Bishan" {:lat 1.350772 :lng 103.839}
   "Boon Lay" {:lat 1.304 :lng 103.701}
   "Bukit Batok" {:lat 1.353 :lng 103.754}
   "Bukit Merah" {:lat 1.277 :lng 103.819}
   "Bukit Panjang" {:lat 1.362 :lng 103.77195}
   "Bukit Timah" {:lat 1.325 :lng 103.791}
   "Central Water Catchment" {:lat 1.38 :lng 103.805}
   "Changi" {:lat 1.357 :lng 103.987}
   "Choa Chu Kang" {:lat 1.377 :lng 103.745}
   "City" {:lat 1.292 :lng 103.844}
   "Clementi" {:lat 1.315 :lng 103.76}
   "Geylang" {:lat 1.318 :lng 103.884}
   "Hougang" {:lat 1.361218 :lng 103.886}
   "Jalan Bahar" {:lat 1.347 :lng 103.67}
   "Jurong East" {:lat 1.326 :lng 103.737}
   "Jurong Island" {:lat 1.266 :lng 103.699}
   "Jurong West" {:lat 1.34039 :lng 103.705}
   "Kallang" {:lat 1.312 :lng 103.862}
   "Lim Chu Kang" {:lat 1.423 :lng 103.717332}
   "Mandai" {:lat 1.419 :lng 103.812}
   "Marine Parade" {:lat 1.297 :lng 103.891}
   "Novena" {:lat 1.327 :lng 103.826}
   "Pasir Ris" {:lat 1.37 :lng 103.948}
   "Paya Lebar" {:lat 1.358 :lng 103.914}
   "Pioneer" {:lat 1.315 :lng 103.675}
   "Pulau Tekong" {:lat 1.403 :lng 104.053}
   "Pulau Ubin" {:lat 1.404 :lng 103.96}
   "Punggol" {:lat 1.401 :lng 103.904}
   "Queenstown" {:lat 1.291 :lng 103.78576}
   "Seletar" {:lat 1.404 :lng 103.869}
   "Sembawang" {:lat 1.445 :lng 103.818495}
   "Sengkang" {:lat 1.384 :lng 103.891443}
   "Sentosa" {:lat 1.243 :lng 103.832}
   "Serangoon" {:lat 1.357 :lng 103.865}
   "Southern Islands" {:lat 1.208 :lng 103.842}
   "Sungei Kadut" {:lat 1.413 :lng 103.756}
   "Tampines" {:lat 1.345 :lng 103.944}
   "Tanglin" {:lat 1.308 :lng 103.813}
   "Tengah" {:lat 1.374 :lng 103.715}
   "Toa Payoh" {:lat 1.334304 :lng 103.856327}
   "Tuas" {:lat 1.294947 :lng 103.635}
   "Western Islands" {:lat 1.205926 :lng 103.746}
   "Western Water Catchment" {:lat 1.405 :lng 103.689}
   "Woodlands" {:lat 1.432 :lng 103.786528}
   "Yishun" {:lat 1.418 :lng 103.839}})

(def BASE_URL "https://api.data.gov.sg/v1/environment")

(defn json-get [url params]
  (let [{:keys [status body error]}
        @(http/get url {:headers {"Content-Type" "application/json"}
                        :query-params params})]
    (if (or error (not (= status 200)))
      (do
        (println "Failed" status error body)
        nil)
      (from-json body))))

(defn nea-json-get [path params]
  (json-get (str BASE_URL path) params))

(defn round-timestamp
  "NEA's timestamps are sometimes slightly off,
   i.e. not exactly on the half-hour / hour mark,
   so this function makes everything neat"
  [forecast-result]
  (assoc-in forecast-result [:items 0 :timestamp] (-> forecast-result
                                                      :items
                                                      first
                                                      :timestamp
                                                      t/get-last-half-hour)))

(defn is-valid-forecast? [forecast]
  (-> forecast :items first :timestamp nil? not))

; Note that forecasts / readings for a certain timestamp only
; become available a few minutes after the timestamp.
; From experience, a 10 minute buffer is sufficient.

; date-time needs to be about 10 minutes after desired forecast time
(defn get-2h-forecast
  ([date-time]
   (let [buffered-date-time (t/get-buffered-last-half-hour date-time)
         result (nea-json-get "/2-hour-weather-forecast" {:date_time buffered-date-time})]
     (l/trace "get-2h-forecast" buffered-date-time)
     (if (is-valid-forecast? result)
       (round-timestamp result)
       nil))))

(defn get-temperature
  ([date-time]
   (l/trace "get-temperature" date-time)
   (nea-json-get "/air-temperature" {:date_time date-time})))

(defn get-rainfall
  ([date-time]
   (l/trace "get-rainfall" date-time)
   (nea-json-get "/rainfall" {:date_time date-time})))

(defn get-area-forecasts-from-2h-forecast [forecast]
  (-> forecast
      :items
      first
      :forecasts))

(defn forecasts-rain? [forecast]
  (case forecast
    ("Windy" "Misty" "Mist" "Fair" "Fair (Day)" "Fog" "Fair (Night)" "Fair & Warm"
             "Hazy" "Slightly Hazy" "Overcast" "Cloudy" "Partly Cloudy" "Partly Cloudy (Day)"
             "Partly Cloudy (Night)" "Sunny" "Strong Winds" "Windy, Cloudy" "Windy, Fair") false
    ("Light Rain" "Drizzle" "Light Showers" "Passing Showers" "Showers"
                  "Heavy Thundery Showers with Gusty Winds" "Heavy Rain" "Heavy Showers"
                  "Moderate Rain" "Strong Winds, Showers" "Strong Winds, Rain"
                  "Thundery Showers" "Windy, Rain" "Windy, Showers"
                  "Heavy Thundery Showers" "Snow" "Snow Showers") true))

(defn within-expected-rainfall-for-forecast?
  "expects a forecast string, e.g. Cloudy and returns a number representing the expected rainfall"
  [forecast rainfall-value]
  (l/trace "Forecast:" forecast "Actual:" rainfall-value)
  (or (and (forecasts-rain? forecast) (> rainfall-value 0))
      (and (not (forecasts-rain? forecast)) (= rainfall-value 0))))

(defn get-nearest-stations
  "takes map of temperature/rainfall stations and region string,
   returns closest stations to the region"
  ([s r] (get-nearest-stations s r 5))
  ([stations-list region-str limit]
   (let [region (get forecast-regions region-str)]
     (->> (seq stations-list)
          (map #(assoc (last %)
                       :station-id (first %)
                       :distance (get-distance region (last %))))
          (sort-by :distance)
          (take limit)))))

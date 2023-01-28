(defproject whether "0.1.0"
  :description "Tells you whether the weather forecast was right or not"
  :url "https://github.com/hueyy/whether-report"
  :min-lein-version "2.0.0"
  :license "AGPL-3.0"
  :dependencies [[com.github.seancorfield/next.jdbc "1.3.847"]
                 [com.taoensso/timbre "5.1.2"]
                 [compojure "1.7.0"]
                 [environ "1.2.0"]
                 [hiccup "1.0.5"]
                 [http-kit "2.5.3"]
                 [metosin/jsonista "0.3.7"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.673"]
                 [org.clojure/core.memoize "1.0.257"]
                 [org.clojure/math.numeric-tower "0.0.5"]
                 [org.xerial/sqlite-jdbc "3.40.0.0"]
                 [ring "1.9.6"]
                 [ring-cors "0.1.13"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.1"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler whether.main/app
         :nrepl {:start? true
                 :port 8888}
         :auto-reload true}
  :main whether.main
  :jvm-opts ["-Xmx4G"]
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}
   :prod  {:main whether.main
           :aot :all}})
(defproject whether "0.1.0"
  :description "Tells you whether the weather forecast was right or not"
  :url "https://github.com/hueyy/whether-report"
  :min-lein-version "2.0.0"
  :license "AGPL-3.0"
  :dependencies [[com.github.seancorfield/next.jdbc "1.2.753"]
                 [com.taoensso/timbre "5.1.2"]
                 [compojure "1.6.2"]
                 [environ "1.2.0"]
                 [hiccup "1.0.5"]
                 [http-kit "2.5.3"]
                 [metosin/jsonista "0.3.4"]
                 [org.clojure/clojure "1.10.3"]
                 [org.clojure/core.async "1.3.618"]
                 [org.clojure/core.memoize "1.0.250"]
                 [org.clojure/math.numeric-tower "0.0.5"]
                 [org.xerial/sqlite-jdbc "3.36.0.3"]
                 [ring "1.9.4"]
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
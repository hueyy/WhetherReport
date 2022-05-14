(ns whether.constants
  (:require [environ.core :refer [env]]))

; NOTE: these vars are filled in during uberjar build, to fix maybe?

(def port (Integer. (env :port 8000)))

(def dev? (= (env :is-production "false") "false"))

(def vite-url (env :vite-url "http://localhost:3001"))

(def manifest-path (env :manifest-path "../frontend/dist/manifest.json"))

(def db-file (env :db-file "whether.sqlite"))
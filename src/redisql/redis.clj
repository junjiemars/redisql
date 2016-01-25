(ns redisql.redis
  (:require [taoensso.carmine :as c :refer (wcar)]
            [clojure.tools.logging :as log])
  (:gen-class))

(def ^:dynamic *config*
  (atom {:pool {}
         :spec {:host "localhost"
                :port 6379}}))

(defmacro c*
  [& body]
  `(c/wcar *config* ~@body))

(defn read-*config*
  "Read config from file and store it in *config*"
  [f]
  (log/info "# loading config from file:" f)
  (try
    (let [c (read-string (slurp f))]
      (swap! *config* merge c))
    (catch RuntimeException e
      (log/error e))))

(defn save-*config*
  "Save *config* to file"
  ([f c] (spit f c)))

(defn ping
  []
  (c* (c/ping)))

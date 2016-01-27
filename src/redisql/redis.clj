(ns redisql.redis
  (:require [taoensso.carmine :as c :refer (wcar)]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io])
  (:gen-class))

(def ^:dynamic *config*
  (atom {:pool {}
         :spec {:host "localhost"
                :port 6379}}))

(def ^:dynamic *lua*
  (atom {:scheme ""}))

(defmacro c*
  [& body]
  `(c/wcar @*config* ~@body))

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

(defn inject-scripts
  []
  (let [l @*lua*
        s (:scheme l)
        f (slurp (io/resource "scheme.lua"))]
    (when (or (empty? s)
              (zero? (first (c* (c/script-exists s)))))
      (let [sha (c* (c/script-load f))]
        (swap! *lua* merge {:scheme sha})))))

(defn make-scheme
  ([s] (c* (c/evalsha s 0 '())))
  ([s n k & args] (c* (c/evalsha s n k args))))


(defn make-table
  [t]
  (make-scheme (:scheme @*lua*) 1 t))

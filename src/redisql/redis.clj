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
  (atom {:scheme ""
         :column ""
         :insert ""}))

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
        m (map vector l)]
    (into {}
          (map (fn [x]
                 (let [i (first x)
                       k (first i)
                       v (second i)
                       f (slurp
                          (io/resource
                           (format "%s.lua" (name k))))]
                   (if (or (empty? v)
                           (zero? (first
                                   (c* (c/script-exists k)))))
                     (let [s (c* (c/script-load f))]
                       (swap! *lua* merge {k s})
                       {k s})
                     {k v})))
               m))))

(defn make-scheme
  ([s] (c* (c/evalsha s 0 '())))
  ([s n k & args] (c* (c/evalsha s n k args))))


(defn make-table
  [t]
  (make-scheme (:scheme @*lua*) 0 t))

(defn make-column
  [t {:keys [NAME] :as k}]
  (when-let [d (c* (c/evalsha (:column @*lua*) 0 t NAME))]
    (c* (c/hmset* (format d (:NAME k)) k))))

(defn make-row
  [t fs vs]
  (let [n (inc (count fs))
        k (conj fs (first t))]
    (println n k vs)
    (c* (c/evalsha (:insert @*lua*) n fs vs))))

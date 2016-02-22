(ns redisql.util
  (:require [clojure.string :as s]))

(def pid 
  (-> (java.lang.management.ManagementFactory/getRuntimeMXBean)
      (.getName)
      (clojure.string/split #"@")
      (first)))

(def host
  (.getCanonicalHostName (java.net.InetAddress/getLocalHost)))

(defn exit
  ([status] (exit status nil))
  ([status msg]
   (when-not (empty? msg)
     (println msg))
   (System/exit status)))

(defn spawn
  ([^Runnable f] (Thread. f))
  ([^Runnable f ^String n] (Thread. f n)))

(defn on-exit
  [f]
  (.addShutdownHook
   (Runtime/getRuntime)
   (spawn #(try
             (f)
             (catch Exception e
               (println e)))
          "#on-exit")))

(defn norm [s]
  (s/upper-case s))

(defn int=
  [x d]
  (if (or (nil? x) (not (integer? x))) d (int x)))

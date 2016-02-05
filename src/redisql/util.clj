(ns redisql.util
  (:require [clojure.string :as s]))

(def pid 
  (-> (java.lang.management.ManagementFactory/getRuntimeMXBean)
      (.getName)
      (clojure.string/split #"@")
      (first)))

(def host
  (.getCanonicalHostName (java.net.InetAddress/getLocalHost)))


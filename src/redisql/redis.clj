(ns redisql.redis
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.pprint :as p])
  (:import (redis.clients.jedis Jedis
                                BinaryJedis
                                JedisPool
                                JedisPoolConfig))
  (:gen-class))

(def ^:dynamic *config*
  (atom {:pool {}
         :spec {:host "localhost"
                :port 6379}}))

(def ^:dynamic *lua*
  (atom {:scheme ""
         :table ""
         :insert ""
         :test ""}))

(def ^:dynamic ^JedisPool *pool*
  (atom (JedisPool.)))

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

(defn ^Jedis borrow []
  (.getResource ^JedisPool @*pool*))

(defmacro in-pool
  [bindings & body]
  (cond
    (= (count bindings) 0)
    `(do ~@body)
    (symbol? (bindings 0))
    `(let ~(subvec bindings 0 2)
       ~(vary-meta (bindings 0) assoc :tag 'Jedis)
       (try
         (in-pool ~(subvec bindings 2) ~@body)
         (finally
           (. ~(bindings 0) close))))))

(defn ping []
  (in-pool [j (borrow)]
           (.ping j)))

(defn script-exists
  [^String s]
  (in-pool [j (borrow)]
           (.scriptExists j s)))

(defn script-load
  [^String s]
  (in-pool [j (borrow)]
           (.scriptLoad j s)))

(defn evalsha
  ([^String s] (evalsha s nil))
  ([^String s ks & args]
   (let [n (count ks)
         v (flatten (seq (concat ks args)))]
     (in-pool [j (borrow)]
              (.evalsha j s n
                        ^"[Ljava.lang.String;"
                        (into-array String (map str v)))))))

(defn exists
  [^String k]
  (in-pool [j (borrow)]
           (.exists j k)))

(defn hmset
  [^String k {:as fs}]
  (in-pool [j (borrow)]
           (.hmset j k ^java.util.Map fs)))

(defn inject-scripts []
  (let [m @*lua*]
    (doseq [k (keys m)]
      (let [f (slurp
               (io/resource
                (format "%s.lua" (name k))))]
        (when-not (script-exists (k m))
          (let [s (script-load f)]
            (swap! *lua* assoc k s)))))
    @*lua*))

(defn make-scheme []
  (let [s (:scheme (if (empty? (:scheme @*lua*))
                     (inject-scripts)
                     @*lua*))]
    (in-pool [j (borrow)]
             (evalsha s))))

(defn make-table
  [t c d]
  (when-let [s (make-scheme)]
    (let [d1 (map (fn [x]
                    (vector (name (first x)) (second x)))
                  (vec d))
          d2 (conj d1 t)]
      (evalsha (:table @*lua*) nil d2))))


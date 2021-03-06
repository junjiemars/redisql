(ns redisql.redis
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [redisql.util :as u])
  (:import (redis.clients.jedis Jedis
                                BinaryJedis
                                JedisPool
                                JedisSentinelPool
                                JedisPoolConfig)
           (redis.clients.util Pool)))

(def ^:private client-name (str "redisql-" u/pid))

(def ^:dynamic ^:private *config*
  (atom {:spec {:host "localhost"
                :port 6379
                :timeout 1000
                :database 0
                :auth nil
                :name client-name}}))

(def ^:dynamic ^:private *lua*
  (atom {:scheme ""
         :table ""
         :insert ""
         :select ""
         :select-eq ""
         :select-comp ""
         :describe ""
         :columns ""
         :test ""}))

(def ^:dynamic ^:private ^Pool *pool* (atom nil))

(defn- read-*config*
  [f]
  (try
    (let [c (read-string f)]
      (swap! *config* merge c))
    (catch RuntimeException e
      (log/error e))))

(defn- save-*config*
  ([f c]
   (try
     (spit f c)
     (catch Exception e
       (log/error e)))))

(defn- ^Jedis borrow []
  (.getResource ^JedisPool @*pool*))

(defn- as-pool
  [{:keys [spec sentinel] :as c}]
  (if-not sentinel
    (JedisPool. (JedisPoolConfig.)
                (:host spec)
                (u/int= (:port spec) 6379)
                (u/int= (:timeout spec) 1000)
                (:auth spec)
                (u/int= (:database spec) 0)
                client-name)
    (JedisSentinelPool. (:master sentinel)
                        (:hosts sentinel)
                        (JedisPoolConfig.)
                        (u/int= (:timout spec) 1000)
                        (:auth spec)
                        (u/int= (:database spec) 0))))

(defn close-pool []
  (when-let [^Pool p @*pool*]
    (when-not (.isClosed p)
      (.close p))))

(defn init-pool
  ([]
   (close-pool)
   (reset! *pool* (as-pool @*config*)))
  ([f]
   (if (empty? f)
     (init-pool)
     (do
       (close-pool)
       (when-let [c (read-*config* f)]
         (reset! *pool* (as-pool c)))))))

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

(defn scripted? []
  (not (empty? (:scheme @*lua*))))

(defn pooled? []
  (when-let [^Pool p @*pool*]
    (not (.isClosed p))))

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

(defn del
  [& args]
  (in-pool [j (borrow)]
           (.del j
                 ^"[Ljava.lang.String;"
                 (into-array String args))))

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
  [t d]
  (when-let [s (make-scheme)]
    (let [t1 t]
      (loop [i d
             v []]
        (if (empty? i)
          v
          (let [d1 (conj (first i) t1)]
            (recur (rest i)
                   (conj v
                         (evalsha (:table @*lua*)
                                  nil
                                  d1)))))))))

(defn insert
  [t c v]
  (let [r1 (map #(vector (u/norm %1) %2) c v)
        r2 (conj r1 t)]
    (evalsha (:insert @*lua*) nil r2)))

(defn select
  [t c w i]
  (let [t1 t
        s1 @*lua*
        rs (if-not (empty? w)
             (let [op (first w)]
               (cond
                 (= "=" op)
                 (evalsha (:select-eq s1) nil t1 w)
                 :else
                 (evalsha (:select-comp s1) nil t1 i w)))
             (evalsha (:select s1) nil t1 i))]
    rs))

(defn describe [t]
  (let [l (:describe @*lua*)]
    (if (nil? t)
      (evalsha l nil)
      (let [t1 (u/norm t)]
        (evalsha l nil t1)))))

(defn columns
  [t]
  (evalsha (:columns @*lua*) nil (u/norm t)))

(ns redisql.redis
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [redisql.util :as u])
  (:import (redis.clients.jedis Jedis
                                BinaryJedis
                                JedisPool
                                JedisPoolConfig)
           (redis.clients.util Pool)))

(def ^:private client-name (str "redisql-" u/pid))

(def ^:dynamic ^:private *config*
  (atom {:pool {}
         :spec {:host "localhost"
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
         :describe ""
         :test ""}))

(def ^:dynamic ^:private ^JedisPool *pool* (atom nil))

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

(defn- norm [s]
  (s/upper-case s))

(defn- int=
  [x d]
  (if (nil? x) d (int x)))

(defn- ^Jedis borrow []
  (.getResource ^JedisPool @*pool*))

(defn- as-pool
  [{:keys [pool spec] :as c}]
  (JedisPool. (JedisPoolConfig.)
              (:host spec)
              (int= (:port spec) 6379)
              (int= (:timeout spec) 1000)
              (:auth spec)
              (int= (:database spec) 0)
              client-name))

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
  (not (nil? @*pool*)))

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
  [t d]
  (when-let [s (make-scheme)]
    (let [t1 (norm t)]
      (loop [i d
             v []]
        (if (empty? i)
          v
          (let [d1 (map (fn [x]
                          (vector (norm (name (first x)))
                                  (norm (second x))))
                        (first i))
                d2 (conj d1 t1)]
            (recur (rest i)
                   (conj v (evalsha (:table @*lua*) nil d2)))))))))

(defn insert
  [t c v]
  (let [r1 (map #(vector (norm %1) %2) c v)
        r2 (conj r1 (norm t))]
    (evalsha (:insert @*lua*) nil r2)))

(defn select
  [t c w i]
  (let [t1 (norm (first t))
        c1 (first c)]
    ;; needs where optimizer
    (evalsha (:select @*lua*) nil t1 i)))

(defn describe
  ([t]
   (let [l (:describe @*lua*)]
     (if (nil? t)
       (evalsha l nil)
       (let [t1 (norm (first t))]
         (evalsha l nil t1))))))

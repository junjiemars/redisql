(ns redisql.sql
  (:require [clojure.tools.logging :as log]
            [instaparse.core :as i]
            [clojure.java.io :as io]
            [taoensso.carmine :as c]
            [redisql.redis :as r]
            [clojure.string :as s])
  (:gen-class))

(def whitespace (i/parser "whitespace = #'\\s+'"))

(def bnf (i/parser (slurp (io/resource "sql.bnf"))
                    :output-format :enlive
                    :auto-whitespace whitespace))

(defn norm
  [s]
  (s/lower-case (s/trim s)))

(defn field-define
  [f]
  (log/debug "# f:" f)
  (loop [f1 f
         m {}]
    (if (empty? f1)
      m
      (let [h (first f1)
            k (:tag h)]
        (recur (rest f1)
               (cond
                 (= :k_not_null k)
                 (into m {:NOT_NULL 1})
                 (= :k_primary_key k)
                 (into m {:PRIMARY_KEY 1})
                 (= :d_default k)
                 (into m {:DEFAULT (first (:content h))})
                 :else m))))))

(defn column-define
  [c]
  (log/debug "# c:" c)
  (loop [x c
         m {}]
    (if (empty? x)
      m
      (do
          (let [c1 (first x)
              k (:tag c1)
              v (s/upper-case (first (:content c1)))]
           (recur (rest x)
                 (cond
                   (= :d_id k)
                   (into m {:NAME v})
                   (= :d_number k)
                   (into m {:TYPE "NUMBER"})
                   (= :d_string k)
                   (into m {:TYPE "STRING"})
                   (= :d_col_constraint k)
                   (into m (field-define (:content c1)))
                   :esle m)))))))

(def vtable {:s (fn [x] nil)
             :insert
             (fn [table columns values]
               (log/debug "# table:" table)
               (log/debug "# columns:" columns )
               (log/debug "# values:" values)
               (let [t (:content table)
                     c (:content columns)
                     v (:content values)]
                 (println t)
                 (println c)
                 (println v)
                 (r/c* (c/sadd (str (first t) "_id")
                               (first v)))))
             :create
             (fn [table columns]
               (log/debug "# table:" table)
               (log/debug "# columns:" columns)
               (let [t (first (:content table))
                     cs (:content columns)]
                 (r/create-table t)
                 (doseq [c cs]
                   (column-define (:content c)))))})

(defn execute
  [sql & args]
  (let [ast (i/parse bnf sql)
        f? (i/failure? ast)]
    (if f?
      (i/get-failure ast)
      (i/transform vtable ast))))


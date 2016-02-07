(ns redisql.sql
  (:require [clojure.tools.logging :as log]
            [instaparse.core :as i]
            [clojure.java.io :as io]
            [redisql.redis :as r]))

(def whitespace (i/parser "whitespace = #'\\s+'"))

(def bnf (i/parser (slurp (io/resource "sql.bnf"))
                   :string-ci true
                   :output-format :enlive
                   :auto-whitespace whitespace))

(def dry-bnf (i/parser (slurp (io/resource "sql.bnf"))
                       :string-ci true
                       :output-format :hiccup
                       :auto-whitespace whitespace))

(defn field-define
  [field]
  (loop [f1 field
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
  [column]
  (loop [x column
         m {:NAME nil :TYPE nil
            :NOT_NULL 0 :PRIMARY_KEY 0 :DEFAULT 0}]
    (if (empty? x)
      m
      (do
          (let [c1 (first x)
              k (:tag c1)
                v (first (:content c1))]
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

(def vtable {:s (fn [& args] args)
             :create
             (fn [table columns]
               (let [t (:content table)
                     cs (:content columns)]
                 {:create {:table (first t)
                           :column (mapv
                                    #(column-define (:content %))
                                    cs)}}))
             
             :insert
             (fn [table columns values]
               (let [t (:content table)
                     c (:content columns)
                     v (:content values)]
                 {:insert {:table (first t)
                           :column c
                           :value v}}))
             
             :select
             (fn [columns table where]
               (let [t (:content table)
                     c (:content columns)
                     w (:content where)]
                 {:select {:table t :column c :where w}}))})

(defn parse
  ([sql dry?]
   (let [ast (i/parse (if dry? dry-bnf bnf) sql)
         f? (i/failure? ast)]
     (if f?
       (i/get-failure ast)
       (if dry?
         ast
         (i/transform vtable ast))))))

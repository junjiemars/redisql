(ns redisql.sql
  (:require [clojure.tools.logging :as log]
            [instaparse.core :as i]
            [clojure.java.io :as io]))

(def whitespace (i/parser "whitespace = #'\\s+'"))

(def bnf (i/parser (slurp (io/resource "sql.bnf"))
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
            k (first h)]
        (recur (rest f1)
               (cond
                 (= :k_not_null k)
                 (into m {:NOT_NULL 1})
                 (= :k_primary_key k)
                 (into m {:PRIMARY_KEY 1})
                 (= :d_default k)
                 (into m {:DEFAULT (first (rest h))})
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
              k (first c1)
                v (first (rest c1))]
           (recur (rest x)
                 (cond
                   (= :d_id k)
                   (into m {:NAME v})
                   (= :d_number k)
                   (into m {:TYPE "NUMBER"})
                   (= :d_string k)
                   (into m {:TYPE "STRING"})
                   (= :d_col_constraint k)
                   (into m (field-define (rest c1)))
                   :esle m)))))))



(def vtable {:s (fn [& args] args)
             :create
             (fn [table columns]
               (let [t (rest table)
                     cs (rest columns)]
                 {:create {:table (first t)
                           :column
                           (mapv
                            #(column-define (rest %))
                            cs)}}))
             
             :insert
             (fn [table columns values]
               (let [t (second table)
                     c (rest columns)
                     v (rest values)]
                 {:insert {:table t
                           :column c
                           :value v}}))
             
             :select
             (fn [columns table where]
               (let [t (rest table)
                     c (rest columns)
                     w (rest where)]
                 {:select {:table (first t)
                           :column c
                           :where w}}))

             :describe
             (fn [& table]
               (let [t (if (nil? table)
                         nil
                         (second (first table)))]
                 {:describe {:table t}}))})

(defn parse
  ([sql dry?]
   (let [b bnf
         ast (i/parse b sql)
         f? (i/failure? ast)]
     (if f?
       {:failure (i/get-failure ast)
        :ast nil}
       (do
         {:failure nil
          :ast (if dry?
                 ast
                 (i/transform vtable ast))})))))

(ns redisql.sql
  (:require [clojure.tools.logging :as log]
            [instaparse.core :as i]
            [clojure.java.io :as io]))

(def whitespace (i/parser "whitespace = #'\\s+'"))

(def bnf (i/parser (slurp (io/resource "sql.bnf"))
                       :string-ci true
                       :output-format :hiccup
                       :auto-whitespace whitespace))

(def ^:private vtable
  {:s (fn [& args] args)
   :create
   (fn [table columns]
     (let [t (rest table)
           cs (rest columns)]
       {:create {:table (first t)
                 :column columns}}))
             
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

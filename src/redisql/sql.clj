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
                    ;:output-format :enlive
                    :auto-whitespace whitespace))

(defn norm
  [s]
  (s/lower-case (s/trim s)))

(def vtable {:insert
             (fn [table columns values]
               (println table)
               (println columns)
               (println values)
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
               (println table)
               (println columns))})



(defn execute
  [sql &args]
  (let [ast (i/parse bnf sql)
        f? (i/failure? ast)]
    (if f?
      (i/get-failure ast)
      (i/transform vtable ast))))

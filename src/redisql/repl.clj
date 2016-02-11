(ns redisql.repl
  (:require [redisql.sql :as sql]
            [clojure.pprint :as p]
            [redisql.redis :as r]))

(defn end-input? [i]
  (let [lb (count (re-seq #"\[" i))
        rb (count (re-seq #"]" i))
        rp (count (re-seq #"\(" i))
        lp (count (re-seq #"\)" i))
        sq (count (re-seq #"'" i))]
    (and (= lp rp) (= lb rb) (even? sq))))

(defn input [i]
  (let [n (str i (read-line))]
    (if (end-input? n)
      n
      (do
        (print "  ..redisql> ")
        (flush)
        (recur (str n "\n"))))))

(defn run-sql
  [ast]
  (let [ks (keys ast)
        k (first ks)
        v (k ast)]
    (condp = k
      :create 
      (r/make-table (:table v) (:column v))
      :insert 
      (r/insert (:table v) (:column v) (:value v))
      :select
      (let [f (fn [c]
                (r/select (:table v) (:column v) (:where v) c))]
        ;; needs iterator
        (rest (f 0))))))

(defn run [dry?]
  (do
    (print "redisql> ")
    (flush))
  (let [i (input "")]
    (when-not (or (= i "quit")
                  (empty? i))
     (try
       (let [s (sql/parse i dry?)]
         (p/pprint
          (if dry?
            s
            (run-sql (first s)))))
       (catch Exception e
         (println "!redisql> " e)))
     (recur dry?))))

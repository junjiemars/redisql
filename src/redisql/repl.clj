(ns redisql.repl
  (:require [redisql.sql :as sql]
            [clojure.pprint :as p]
            [redisql.redis :as r]
            [redisql.bridge :as b]
            [redisql.util :as u]
            [clojure.string :as s]))

(def normal-prompt "redisql> ")
(def error-prompt "redisql# ")
(def indent-prompt "..redisql> ")
(def quit-prompt "quit")
(def cmd-pattern #"^\s*(^:[a-zA-Z]+)(\s+(\w+))?\s*;")

(defn input [p]
  (let [c (read-line)
        n (str p c)]
    (cond
      (nil? c) (u/exit 0 quit-prompt)
      (empty? n) (do
                   (print normal-prompt)
                   (flush)
                   (recur n))
      (and (not (empty? n)) (= \; (last n))) n
      :else
      (do
        (print indent-prompt)
        (flush)
        (recur (str n \newline))))))

(declare cmd)

(defn run
  ([dry] (run dry nil))
  ([dry conf]
   (do
     (print normal-prompt)
     (flush))
   (let [i (input "")]
     (when-not (empty? i)
       (try
         (when-not (cmd i)
           (let [s (b/cross i dry conf)]
             (doseq [s1 s]
               (p/pprint s1))))
         (catch Exception e
           (println error-prompt e)))
       (recur dry conf)))))

(defn cmd [s]
  (when-let [cs (re-find cmd-pattern s)]
    (when-let [c (second cs)]
      (when-let [r (cond 
                     (= ":quit" c) (u/exit 0)
                     :else false)]
        true))))

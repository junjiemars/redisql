(ns redisql.repl
  (:require [redisql.sql :as sql]
            [clojure.pprint :as p]
            [redisql.redis :as r]
            [redisql.bridge :as b]
            [redisql.util :as u]))

(def normal-prompt "redisql> ")
(def error-prompt "redisql# ")
(def indent-prompt "..redisql> ")
(def quit-prompt "quit")

(defn input [p]
  (let [c (read-line)
        n (str p c)]
    (cond
      (nil? c) (u/exit 0 quit-prompt)
      (empty? n) (do
                   (print normal-prompt)
                   (flush)
                   (recur n))
      (= quit-prompt n) (u/exit 0)
      (and (not (empty? n)) (= \; (last n))) n
      :else
      (do
        (print indent-prompt)
        (flush)
        (recur (str n \newline))))))

(defn run
  ([dry] (run dry nil))
  ([dry conf]
   (do
     (print normal-prompt)
     (flush))
   (let [i (input "")]
     (when-not (empty? i)
       (try
         (let [s (b/cross i dry conf)]
           (p/pprint s))
         (catch Exception e
           (println error-prompt e)))
       (recur dry conf)))))

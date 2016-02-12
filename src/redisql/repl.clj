(ns redisql.repl
  (:require [redisql.sql :as sql]
            [clojure.pprint :as p]
            [redisql.redis :as r]
            [redisql.bridge :as b]
            [redisql.util :as u]))

(def normal-prompt "redisql> ")
(def error-prompt "redisql# ")
(def indent-prompt "..redisql> ")

(defn input [p]
  (let [c (read-line)
        n (str p c)]
    (cond
      (nil? c) (u/exit 0 "quit")
      (empty? n) (do
                   (print normal-prompt)
                   (flush)
                   (recur n))
      (and (not (empty? n)) (= \; (last n))) n

      :else
      (do
        (print indent-prompt)
        (flush)
        (recur (str n "\n"))))))

(defn run [dry?]
  (do
    (print normal-prompt)
    (flush))
  (let [i (input "")]
    (when-not (or (= i "quit")
                  (empty? i))
     (try
       (let [s (b/cross i dry?)]
         (p/pprint s))
       (catch Exception e
         (println error-prompt e)))
     (recur dry?))))

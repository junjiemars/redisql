(ns jql.cli
  (:use [clojure.tools.cli :refer [parse-opts]])
  (:use [clojure.tools.trace])
  (:require [clojure.string :as string])
  (:import (java.net InetAddress))
  (:gen-class))

(declare parse-cli-specs)

(def cli-specs
  [["-H" "--host HOST" "host name"
    :default (InetAddress/getByName "localhost")]
   ["-p" "--port PORT" "port number"
    :default 6379
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "port number must be 0 and 65536"]]
   ["-h" "--help"]])

(defn parse-cli-specs
  [args]
  (let [{:keys [options arguments summary errors]}
        (parse-opts (vec args) cli-specs)]
    (cond
     (not (empty? arguments)) (trace arguments))
    (println options)
    (println (string/lower-case arguments))
    (println summary)
    (println errors)))



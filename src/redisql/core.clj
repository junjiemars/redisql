(ns redisql.core
  (:use [clojure.tools.cli :refer [parse-opts]])
  (:require [clojure.string :as string])
  (:use [clojure.pprint])
  (:use [clojure.tools.trace])
  (:gen-class))

(def ^:dynamic
  *current-implementation*)

(def conf "resources/conf.clj")

(def cli-specs
  [["-h" "--help"]
   ["-H" "--host host" "redis host address"
    :default "localhost"]
   ["-p" "--port port" "redis port number"
    :default 6379
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "port number between 0 and 65536"]]])

(defn- cli-usage
  []
  (println "usage: [-h|--help] [-H|--host] [-p|--post]")
  (println (str \tab "--help: help"))
  (println (str \tab "--host: redis host address"))
  (println (str \tab "--port: port number")))

(defn- cli-summary
  ([] (cli-usage))
  ([errors] (println errors)
   (cli-usage)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options arguments summary errors]}
        (parse-opts (vec args) cli-specs)]
    (cond
     (not (empty? errors)) (cli-summary
                            (string/lower-case (apply str errors)))
     (not (empty? arguments)) (cli-summary
                               (str "unknown arguments:" arguments))
     (:help options) (cli-summary)
     :else options)))

(derive ::bash ::common)
(derive ::batch ::common)

(defmulti emit
  (fn [form]
    [*current-implementation* (class form)]))

(defmethod emit
  [::bash clojure.lang.PersistentList]
  [form]
  (case (name (first form))
    "println" (str "echo " (second form))
    nil))

(defmethod emit
  [::batch clojure.lang.PersistentList]
  [form]
  (case (name (first form))
    "println" (str "ECHO " (second form))
    nil))

(defmethod emit
  [::common java.lang.String]
  [form]
  form)

(defmethod emit
  [::common java.lang.Long]
  [form]
  (str form))

(defmethod emit
  [::common java.lang.Double]
  [form]
  (str form))

(defmacro script [form]
  `(emit '~form))


(defmacro with-implementation
  [impl & body]
  `(binding [*current-implementation* ~impl]
     ~@body))

(defn save-conf
  [c m]
  (spit c (pr-str m)))

(defn read-conf
  [c]
  (read-string (slurp c)))



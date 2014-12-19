(ns jql.cli
  (:use [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(declare parse-cli-specs)

(def cli-specs
  [["-H" "--host" "host name"
    :default "localhost"]
   ["-p" "--port" "port number"
    :default 6379]
   ["-h" "--help"]])

(defn parse-cli-specs
  [& args]
  (let [{:keys [options arguments summary errors]}
        (parse-opts args cli-specs)]
    (println options)))

(def x 1)

(defn f [a]
  (let [b (* a 20)]
    (println (+ b x))))





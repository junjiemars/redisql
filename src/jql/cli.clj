(ns jql.cli
  (:use [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(declare parse-cli-specs)

(def cli-specs
  [["-H" "--host" "host name"
    :default "localhost"]
   ["-p" "--port" "port number"
    :defautl 6379]
   ["-h" "--help"]])

(defn parse-cli-specs
  [& args]
  (let [{:keys [options]} (parse-opts args cli-specs)]
    (cond
     (:help options) (println "i'm help"))))




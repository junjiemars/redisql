(ns jql.cli
  (:use [clojure.tools.cli :refer [parse-opts]])
  (:use [clojure.tools.trace])
  (:gen-class))

(declare parse-cli-specs)

(def cli-specs
  [["-H" "--host host" "host name"
    :default "localhost"]
   ["-p" "--port port" "port number"
    :default 6379]
   ["-h" "--help"]])

(defn parse-cli-specs
  [args]
  (let [{:keys [options arguments summary errors]}
        (parse-opts (vec args) cli-specs)]
    (println options)
    (println arguments)
    (println summary)
    (println errors)))



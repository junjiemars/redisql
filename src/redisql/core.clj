(ns redisql.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.tools.logging :as log])
  (:gen-class))

(defn exit
  [status msg]
  (println msg)
  (System/exit status))

(def cli-options
  [["-e" "--eval input" "Evalute input"
    :default ""
    :parse-fn #(str %)]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(defn -main
  "Evalute input then output"
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 summary)
      errors (exit 1 (str "! error: "
                          args \newline errors))
      (:eval options)
      (let [input (:eval options)]
        (println input)
        (log/debug input))

      :else (exit 1 summary))))



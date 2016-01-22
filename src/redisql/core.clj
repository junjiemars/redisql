(ns redisql.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [instaparse.core :as i])
  (:gen-class))

(defn exit
  [status msg]
  (println msg)
  (System/exit status))

(def cli-options
  [["-f" "--file BNF" "BNF file"
    :default "sample.bnf"
    :validate [#(.exists (io/as-file %))
               "BNF file not found"]]
   ["-e" "--eval input" "Evalute input"]
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
      (let [input (:eval options)
            bnf (slurp (:file options))]
        (println input)
        (println bnf)
        (println "------------------")
        ((i/parser bnf) input)
        (log/debug input))

      :else (exit 1 summary))))



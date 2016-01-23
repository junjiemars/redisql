(ns redisql.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [instaparse.core :as i])
  (:gen-class))

(defn exit
  [status msg]
  (println msg)
  (System/exit status))

(defn cli-validate-file-opt
  [o]
  (if (= \@ (first o))
    (.exists (io/as-file (subs o 1)))
    true))

(defn cli-parse-file-arg
  [a]
  (if (= \@ (first a))
    (slurp (subs a 1))
    a))

(def cli-options
  [["-b" "--bnf INPUT" "BNF input/@file"
    :default "@sample.bnf"
    :validate [cli-validate-file-opt
               "BNF file not found"]
    :parse-fn cli-parse-file-arg]
   ["-e" "--eval INPUT" "Evalute input/@file"
    :validate [cli-validate-file-opt
               "Eval file not found"]
    :parse-fn cli-parse-file-arg]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(defn -main
  "Evalute input with BNF then output"
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 summary)
      errors (exit 1 (str "! error: "
                          args \newline errors))
      (:eval options)
      (let [input (:eval options)
            bnf (:bnf options)]
        (println "BNF:----------")
        (println bnf \newline)
        (println "EVAL:---------")
        (println input \newline)
        (println "OUT:----------")
        (println ((i/parser bnf) input)))

      :else (exit 1 summary))))



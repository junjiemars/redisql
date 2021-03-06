(ns redisql.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.pprint :as p]
            [redisql.util :as u]
            [redisql.repl :as repl]
            [redisql.bridge :as b]
            [instaparse.core :as i]
            [redisql.sql :as sql])
  (:gen-class))

(defn parse
  [bnf input]
  (let [p (i/parser bnf :auto-whitespace sql/whitespace)
        out (i/parse p input)]
    (if (i/failure? out)
      (i/get-failure out)
      out)))

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
    ;:default (io/resource "sample.bnf")
    :validate [cli-validate-file-opt
               "BNF file not found"]
    :parse-fn cli-parse-file-arg]
   ["-e" "--eval INPUT" "Evalute input/@file"
    :validate [cli-validate-file-opt
               "Eval file not found"]
    :parse-fn cli-parse-file-arg]
   ["-s" "--sql SQL" "SQL input/@file"
    :validate [cli-validate-file-opt
               "SQL file not found"]
    :parse-fn cli-parse-file-arg]
   ["-d" "--dry-run" "SQL's AST only: -d, AST transformed: -dd"
    :id :dry
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-c" "--conf Redis" "Redis client conf file"
    :validate [cli-validate-file-opt
               "Redis client conf not found"]
    :parse-fn cli-parse-file-arg]
   ["-i" "--repl" "REPL mode"
    :id :repl
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
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
      (:help options) (u/exit 0 summary)
      errors (u/exit 1 (str "! error: "
                          args \newline errors))
      (:eval options)
      (let [input (:eval options)
            bnf (:bnf options)]
        (println "BNF:----------")
        (println bnf \newline)
        (println "EVAL:---------")
        (println input \newline)
        (println "OUT:----------")
        (p/pprint (parse bnf input)))

      (:sql options)
      (let [s (:sql options)
            d (:dry options)
            c (:conf options)]
        (time (p/pprint (b/cross s d c))))

      (:repl options)
      (let [d (:dry options)
            c (:conf options)]
        (u/on-exit (fn [] (println "Bye!")))
        (repl/run d c))

      :else (u/exit 1 summary))))

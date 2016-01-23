(ns redisql.core-test
  (:require [clojure.test :refer :all]
            [redisql.core :refer :all]))

(deftest test-cli-parsing
  (testing "cli-validate-file-opt"
    (is (true? (cli-validate-file-opt "sample.bnf")))
    (is (true? (cli-validate-file-opt "@sample.bnf"))))

  (testing "cli-parse-file-arg"
    (is (= "sample.bnf" (cli-parse-file-arg "sample.bnf")))
    (is (< 0 (count (cli-parse-file-arg "@sample.bnf"))))))

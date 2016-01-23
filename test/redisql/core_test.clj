(ns redisql.core-test
  (:require [clojure.test :refer :all]
            [redisql.core :refer :all]))

(deftest test-cli-parsing
  (testing "cli-validate-file-opt"
    (let [s "sample.bnf"
          f "@sample.bnf"]
      (is (true? (cli-validate-file-opt s)))
      (is (true? (cli-validate-file-opt f)))))

  (testing "cli-parse-file-arg"
    (let [s "sample.bnf"
          f "@sample.bnf"]
      (is (= s (cli-parse-file-arg s)))
      (is (< 0 (count (cli-parse-file-arg f)))))))

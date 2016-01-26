(ns redisql.core-test
  (:require [clojure.test :refer :all]
            [redisql.core :refer :all]))

(deftest test-cli-parse
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

(deftest test-bnf-parse
  (testing "parse"
    (let [bnf "S=AB*
               AB=A B
               A='a'+
               B='b'+"
          i1 "aaabb"
          v1 [:S [:AB [:A "a" "a" "a"] [:B "b" "b"]]]]
      (is (= v1 (parse bnf i1))))))

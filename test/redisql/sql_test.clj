(ns redisql.sql-test
  (:require [clojure.test :refer :all]
            [redisql.sql :refer :all]
            [instaparse.core :as i]))

(deftest test-sql-parse
  (testing "insert into statement"
    (let [s " insert into T1 (id, last_name, salary)
             values (101, 'King', 17000) "
          o (i/parse bnfp (norm s))
          f? (i/failure? o)]
      (when f? (println (i/get-failure o)))
      (is (not f?))))

  (testing "create table statement"
    (let [s "create table T1 (
               ID number(4,0) not null PRIMARY key,
               last_name varchar2(30), not null
               salary number(6,2) default 0.0"
          o (i/parse bnfp (norm s))
          f? (i/failure? o)]
      (when f? (println (i/get-failure o)))
      (is (not f?)))))

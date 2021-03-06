(ns redisql.sql-test
  (:require [clojure.test :refer :all]
            [redisql.sql :refer :all]
            [instaparse.core :as i]))

(def dry-create= {:failure nil,
                  :ast
                  [:s
                   [:create
                    [:n_table "T1"]
                    [:l_d_column
                     [:d_column
                      [:d_id "ID"]
                      [:d_type "number"]
                      [:d_col_constraint [:k_primary_key]]]
                     [:d_column
                      [:d_id "last_name"]
                      [:d_type "varchar2"]
                      [:d_col_constraint [:d_not_null]]]
                     [:d_column
                      [:d_id "salary"]
                      [:d_type "number"]
                      [:d_col_constraint [:d_default "0.0"]]]]]]})

(def dry-insert= {:failure nil,
                  :ast
                  [:s
                   [:insert
                    [:n_table "T1"]
                    [:l_column "id" "last_name" "salary"]
                    [:l_value "101" "King" "17000"]]]})

(deftest test-sql-dry-parse
    (testing "create table statement"
    (let [s "create table T1 (
               ID number(4,0) primary key,
               last_name varchar2(30) not null
               salary number(6,2) default 0.0);"
          d (parse s true)]
      (is (= d dry-create=))))

  (testing "insert into statement"
    (let [s "insert into T1 (id, last_name, salary)
             values (101, 'King', 17000);"
          d (parse s true)]
      (is (= d dry-insert=)))))

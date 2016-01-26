(ns redisql.redis-test
  (:require [clojure.test :refer :all]
            [redisql.redis :refer :all]
            [clojure.java.io :as io]))

(deftest test-*config*
  (testing "save-*config*"
    (let [f "redis.conf"
          c @*config*]
      (save-*config* f c)
      (is (true? (.exists (io/as-file f))))))

  (testing "read-*config*"
    (let [f "redis.conf"]
      (is (not (nil? (read-*config* f))))
      (io/delete-file f))))

(deftest test-redis
  ; Redis should on
  (testing "inject-scheme-script"
    (is (not (nil? (inject-scheme-script)))))

  (testing "create-scheme"
    (let [s (:scheme @*lua*)]
      (is (vector? (create-scheme s)))))

  (testing "create-table"
    (let [t "X"]
      (is (vector? (create-table t))))))


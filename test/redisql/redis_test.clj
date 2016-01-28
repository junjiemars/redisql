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
  (testing "inject-scripts"
    (is (not (nil? (inject-scripts)))))

  (testing "make-scheme"
    (when-let [s (:scheme (inject-scripts))]
      (is (vector? (make-scheme s)))))

  (testing "make-table"
    (when (inject-scripts)
        (let [t "X"]
          (is (vector? (make-table t)))))))


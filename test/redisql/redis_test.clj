(ns redisql.redis-test
  (:require [clojure.test :refer :all]
            [redisql.redis :refer :all]
            [clojure.java.io :as io]))

(deftest test-jedis
  ; Redis should on
  (testing "init-pool"
    (is (not (nil? (init-pool)))))
  
  (testing "ping"
    (is (= "PONG" (ping))))

  (testing "script-load/evalsha"
    (let [s "return {'a', 1}"
          h "4e7446071a54ac87de3e16bf16860ccbb778d137"]
      (is (= h (script-load s)))
      (is (= ["a" 1] (evalsha h)))))

  (testing "hmset"
    (let [k "X"
          fs {"a" "123" "b" "234"}]
      (is (= "OK" (hmset k fs))))))

(deftest test-redis
  ; Redis should on
  (init-pool)
  
  (testing "inject-scripts"
    (let [m (inject-scripts)]
      (is (pos? (count (:scheme m))))
      (is (pos? (count (:table m))))))

  (testing "make-scheme"
    (let [v [0 "create redisql's scheme ok"]]
      (is (= v (make-scheme)))))

  (comment
    (testing "make-table"
      (let [t "X"]
        (is (vector? (make-table t)))))))


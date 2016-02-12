(ns redisql.bridge
  (:require [redisql.redis :as r]
            [redisql.sql :as s]))


(defn cross
  [sql dry?]
  (let [{:keys [failure ast]} (s/parse sql dry?)]
    (cond
      failure failure
      dry? ast
      :else
      (do
        (when-not (r/pooled?) (r/init-pool))
        (when-not (r/scripted?) (r/inject-scripts))
        (let [a (first ast)
              ks (keys a)
              k (first ks)
              v (k a)]
          (condp = k
            :create 
            (r/make-table (:table v) (:column v))
            :insert 
            (r/insert (:table v) (:column v) (:value v))
            :select
            (let [f (fn [c]
                      (r/select (:table v) (:column v) (:where v) c))]
              ;; needs iterator
              (rest (f 0)))))))))


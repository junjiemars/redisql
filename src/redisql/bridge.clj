(ns redisql.bridge
  (:require [redisql.redis :as r]
            [redisql.sql :as s]))


(defn cross
  ([sql dry? conf] (cross sql dry? conf 0))
  ([sql dry? conf cursor]
   (let [{:keys [failure ast]} (s/parse sql dry?)]
     (cond
       failure failure
       dry? ast
       :else
       (do
         (when-not (r/pooled?) (r/init-pool conf))
         (when-not (r/scripted?) (r/inject-scripts))
         (let [a (first ast)
               ks (keys a)
               k (first ks)
               v (k a)]
           (condp = k
             :create
             (let [mk (r/make-table (:table v) (:column v))]
               mk)
             
             :insert
             (let [i (r/insert (:table v) (:column v) (:value v))]
               i)
             
             :select
             (let [f (fn [c]
                       (r/select (:table v) (:column v) (:where v) c))
                   l (f cursor)]
               (rest (f cursor))))))))))

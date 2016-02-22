(ns redisql.bridge
  (:require [redisql.redis :as r]
            [redisql.sql :as sql]
            [clojure.zip :as z]
            [clojure.string :as s]
            [redisql.util :as u]))

(def column-define {:name nil
                    :type nil
                    :not-nil? 0 :pk? 0 :default 0})

(defn- visitor [zipper fn]
  (loop [z zipper
         f fn]
    (if (z/end? z)
      (z/root z)
      (recur (z/next (f z)) f))))

(defn prefix [[id op val]]
  [op id val])

(defn- define-column [zipper]
  (let [z zipper
        n (z/node z)]
    (cond 
      (= :d_id n)
      (let [did (z/edit z (fn [_] "NAME"))]
        (z/edit (z/right did) (fn [v] (s/upper-case v))))
      (= :d_type n)
      (z/edit z (fn [_] "TYPE"))
      (= "number" n)
      (z/edit z (fn [_] "NUMBER"))
      (some #(= % n) ["varchar" "varchar2"])
      (z/edit z (fn [_] "STRING"))
      (= :k_primary_key n)
      (let [u2 (z/up (z/up z))]
        (z/edit u2 (fn [_] ["PRIMARY_KEY" 1])))
      (= :k_identity n)
      (let [u2 (z/up (z/up z))]
        (z/edit u2 (fn [_] ["PRIMARY_KEY" 3])))
      (= :d_not_null n)
      (let [u2 (z/up (z/up z))]
        (z/edit u2 (fn [_] ["NOT_NULL" 1])))
      (= :d_default n)
      (let [u2 (z/up (z/up z))
            d1 (z/node (z/right z))]
        (z/edit u2 (fn [_] ["DEFAULT" d1])))
      :else z)))

(defn- eval-where [zipper]
  (let [z zipper
        n (z/node z)]
    (cond
      (= :comp_op n)
      (let [u1 (z/up z)
            n1 (z/node (z/right z))]
        (z/edit u1 (fn [_] n1)))
      (= :d_id n)
      (let [u1 (z/up z)
            n1 (z/node (z/right z))]
        (z/edit u1 (fn [_] (u/norm n1))))
      :else z)))

(defn- where? [w]
  (when-let [w1 (first w)]
    (let [w2 (visitor (z/vector-zip w1) eval-where)
          w3 (rest (first (rest w2)))]
      (prefix (vec w3)))))

(defn cross
  ([sql dry] (cross sql dry nil))
  ([sql dry conf] (cross sql dry conf 0))
  ([sql dry conf cursor]
   (let [d? (= 1 dry)
         {:keys [failure ast]} (sql/parse sql d?)]
     (cond
       failure failure
       (pos? dry) ast
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
             (let [z1 (z/vector-zip (:column v))
                   v1 (visitor z1 define-column)
                   c1 (rest v1)
                   v2 (map flatten (map #(rest %) c1))]
               (r/make-table (u/norm (:table v)) v2))
             
             :insert
             (let [i (r/insert (u/norm (:table v))
                               (:column v)
                               (:value v))]
               i)
             
             :select
             (let [w (where? (:where v))]
               (r/select (u/norm (:table v))
                         (:column v)
                         w
                         cursor))

             :describe
             (let [s (r/describe (:table v))]
               s))))))))

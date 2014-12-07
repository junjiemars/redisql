(ns jql.core
  (:gen-class))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(derive ::bash ::common)
(derive ::batch ::common)

(defmulti emit
  (fn [form]
    [*current-implementation* (class form)]))

(def ^{:dynamic true}
  "The current script language implementation to generate"
  *current-implementation*)

(defmethod emit
  [::bash clojure.lang.PersistentList]
  [form]
  (case (name (first form))
    "println" (str "echo " (second form))
    nil))

(defmethod emit
  [::batch clojure.lang.PersistentList]
  [form]
  (case (name (first form))
    "println" (str "ECHO " (second form))
    nil))

(defmethod emit
  [::common java.lang.String]
  [form]
  form)

(defmethod emit
  [::common java.lang.Long]
  [form]
  (str form))

(defmethod emit
  [::common java.lang.Double]
  [form]
  (str form))



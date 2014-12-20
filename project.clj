(defproject jql "0.1.0-SNAPSHOT"
  :description "redis query language like sql"
  :url "https://github.com/junjiemars/jql"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;:main ^:skip-aot jql.core
  :main jql.core
  :source-paths ["src"]
  :test-paths ["test"]
  :target-path "target/%s"
  :dependencies [[org.clojure/clojure "1.6.0"]])

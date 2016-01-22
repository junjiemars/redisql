(defproject redisql "0.1.0-SNAPSHOT"
  :description "redis query language like sql"
  :url "https://github.com/junjiemars/redisql"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;:main ^:skip-aot redisql.core
  :main redisql.core
  :source-paths ["src"]
  :test-paths ["test"]
  :target-path "target/%s"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/tools.trace "0.7.8"]])

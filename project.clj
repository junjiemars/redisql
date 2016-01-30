(defproject redisql "0.1.0"
  :description "redis query language like sql"
  :url "https://github.com/junjiemars/redisql"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.1"]
                 [instaparse "1.4.1"]
                 [redis.clients/jedis "2.8.0"]]
  :main redisql.core
  :profiles {:dev {:jvm-ops ["-Droot-level=DEBUG"]
                   :global-vars {*warn-on-reflection* true}
                   :javac-options ["-g"]}
             :uberjar {:aot :all
                       :jvm-opts ["-Droot-level=INFO"]
                       :global-vars {*warn-on-reflection* true}
                       :javac-options ["-g"]}})

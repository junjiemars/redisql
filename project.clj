(defproject redisql "0.1.0"
  :description "Play Redis as a rational store via SQL"
  :url "https://github.com/junjiemars/redisql"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.1"]
                 [instaparse "1.4.1"]
                 [redis.clients/jedis "2.8.0"]]
  :main ^:skip-aot redisql.core
  :target-path "target/%s"
  :profiles {:dev {:jvm-ops ["-Droot-level=DEBUG"]
                   :global-vars {*warn-on-reflection* true}
                   :javac-options ["-g"]}
             :uberjar {:aot :all
                       :jvm-opts ["-Droot-level=INFO"]
                       :global-vars {*warn-on-reflection* true}
                       :javac-options ["-g"]}})

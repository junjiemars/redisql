(defproject jql "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;:main ^:skip-aot jql.core
  :main jql.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/clojure "1.6.0"]
                                  [org.clojure/tools.cli "0.3.1"]
                                  [org.clojure/tools.trace "0.7.8"]]}
             :pro {:dependencies [[org.clojure/clojure "1.6.0"]
                                  [org.clojure/tools.cli "0.3.1"]]}})

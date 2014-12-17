(defproject jql "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/junjiemars/jql"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;:main ^:skip-aot jql.core
  :main jql.core
  :target-path "target/%s"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/tools.trace "0.7.8"]
                                  #_ ([spyscope "0.1.5"])
                                  ]}
             :pro {:aot
                   :all
                   :dependencies []}})

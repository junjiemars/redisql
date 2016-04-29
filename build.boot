(import 'java.net.InetAddress)

(set-env!
 :project 'redisql
 :version "0.1.0"
 :description "Play Redis as a rational store via SQL"
 :url "http://github.com/junjiemars/redisql"
 :license {"Eclipse Public License"
           "http://www.eclipse.org/legal/epl-v10.html"}
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.1"]
                 [instaparse "1.4.1"]
                 [redis.clients/jedis "2.8.0"]
                 [adzerk/boot-test "1.1.1" :scope "test"]]
 :source-paths #{"src" "test"}
 :resource-paths #{"resources"}
 ;:javac-options " -g -source 1.8 -target 1.6 "
 :javac-options " -g " )

(require '[adzerk.boot-test :refer :all])
(set! *warn-on-reflection* true)

(defn hostname
  "Return hostname"
  []
  (.getHostName (InetAddress/getLocalHost)))

(defn githead
  "Return the commit hash of current git HEAD"
  []
  (let [{:keys [exit out]}
        (clojure.java.shell/sh
         "git" "describe" "--tags" "--always" "HEAD")]
    (if (zero? exit)
      (subs out 0 (dec (count out)))
      "0000000")))

(task-options!
 aot {:all true}
 pom {:project (get-env :project)
      :version (get-env :version)
      :description (get-env :description)
      :url (get-env :url)
      :license (get-env :license)}
 uber :as-jars
 jar {:main 'redisql.core
      :manifest {"Build-By" (str
                             (System/getProperty "user.name")
                             "@"
                             (hostname))
                 "Build-Version" (str (get-env :version)
                                      "-"
                                      (githead))
                 "Build-Date" (str (java.util.Date.))}})

(deftask build
  "Build the snail jar"
  [d dev bool "build for development"
   p pro bool "build for production"]
  (if (not pro)
    (do
      (System/setProperty "root-level" "DEBUG")
      (comp
       (javac (get-env :javac-options))
       (aot) (pom) (jar) (target)))
    (do
      (System/setProperty "root-level" "INFO")
      (comp
       (javac (get-env :javac-options))
       (aot)
       (pom)
       (uber)
       (jar :file (format "%s-%s-pro.jar"
                          (get-env :project)
                          (get-env :version)))
       (target)))))


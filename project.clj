(defproject reddit-digest "0.1.0-SNAPSHOT"
  :description "Digest for Reddit. Retrieves top posts for a set of subreddits and sends a newsletter."
  :url "https://github.com/SvenvDam/reddit-digest"
  :license {:name "MIT"
            :url  "https://mit-license.org/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-http "3.10.3"]
                 [org.clojure/data.json "1.0.0"]
                 [cheshire "5.10.0"]
                 [com.sendgrid/sendgrid-java "4.0.1"]
                 [environ "1.2.0"]]
  :main ^:skip-aot reddit-digest.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :test {:resource-paths ["test-resources"]}}
  :uberjar-name "reddit-digest.jar")

(defproject org.cyverse/heuristomancer "2.8.7-SNAPSHOT"
  :description "Clojure library for attempting to guess file types."
  :url "https://github.com/cyverse-de/heuristomancer"
  :license {:name "BSD Standard License"
            :url "http://www.iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :profiles {:dev {:resource-paths ["test-data"]}}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [org.clojure/data.csv "1.1.0"]
                 [org.clojure/tools.cli "1.1.230"]
                 [org.clojure/tools.logging "1.3.0"]
                 [instaparse "1.5.0"]]
  :plugins [[jonase/eastwood "1.4.3"]
            [lein-ancient "0.7.0"]
            [test2junit "1.4.4"]]
  :aot [heuristomancer.core]
  :main heuristomancer.core)

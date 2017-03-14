(defproject org.cyverse/heuristomancer "2.8.2"
  :description "Clojure library for attempting to guess file types."
  :url "https://github.com/cyverse-de/heuristomancer"
  :license {:name "BSD Standard License"
            :url "http://www.iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :profiles {:dev {:resource-paths ["test-data"]}}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [instaparse "1.4.1"]]
  :plugins [[test2junit "1.2.2"]]
  :aot [heuristomancer.core]
  :main heuristomancer.core)

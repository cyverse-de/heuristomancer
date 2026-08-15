(defproject org.cyverse/heuristomancer "2.8.8"
  :description "Clojure library for attempting to guess file types."
  :url "https://github.com/cyverse-de/heuristomancer"
  :license {:name "BSD Standard License"
            :url "http://www.iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :profiles {:dev {:resource-paths ["test-data"]}}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  ;; Fail the build on a new dependency conflict rather than printing a
  ;; warning nobody reads.
  :pedantic? :abort
  :dependencies [[org.clojure/clojure "1.12.5"]
                 [org.clojure/data.csv "1.1.1"]
                 [org.clojure/tools.cli "1.4.256"]
                 [org.clojure/tools.logging "1.3.1"]
                 [instaparse "1.5.0"]]
  :plugins [[jonase/eastwood "1.4.3"]
            [lein-ancient "1.0.0"]
            [test2junit "1.4.4"]]
  :aot [heuristomancer.core]
  :main heuristomancer.core)

(defproject pho-diff "0.1.0-SNAPSHOT"
  :description "Visually compare the phonetic inventories of two languages."
  :url "https://github.com/sgepigon/pho-diff"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[enlive "1.1.6"]
                 [me.raynes/conch "0.8.0"]
                 [org.clojure/clojure "1.9.0"]]
  :profiles {:dev {:dependencies [[expound "0.4.0"]
                                  [orchestra "2017.11.12-1"]
                                  [org.clojure/test.check "0.10.0-alpha2"]
                                  [spec-provider "0.4.9"]]}}
  :main ^:skip-aot pho-diff.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

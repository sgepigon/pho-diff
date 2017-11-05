(defproject pho-diff "0.1.0-SNAPSHOT"
  :description "Compare the phonetic inventory of two languages."
  :url "https://github.com/sgepigon/pho-diff"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta4"]
                 [me.raynes/conch "0.8.0"]]
  :main ^:skip-aot pho-diff.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

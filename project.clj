(defproject pho-diff "0.1.0-SNAPSHOT"
  :description "Compare the phonetic inventory of two languages."
  :url "https://github.com/sgepigon/pho-diff"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[enlive "1.1.6"]
                 [expound "0.3.4"]
                 [me.raynes/conch "0.8.0"]
                 [org.clojure/clojure "1.9.0-RC1"]
                 [org.clojure/test.check "0.10.0-alpha2"]]
  :main ^:skip-aot pho-diff.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

(ns pho-diff.core
  (:gen-class)
  (:require [clojure.java.shell :as shell]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as spec.gen]
            [expound.alpha :as expound]
            [me.raynes.conch :as conch]
            [orchestra.spec.test :as spec.test]
            [pho-diff.diff :as diff]
            [pho-diff.scrape :as scrape]
            [spec-provider.provider :as provider]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

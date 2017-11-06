(ns pho-diff.core
  (:gen-class)
  (:require [clojure.java.shell :as shell]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]
            [me.raynes.conch :as conch]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def english-cons "resources/inventory")


(conch/with-programs [ls] (ls english-cons))

(conch/programs convert)

(shell/sh "ls" english-cons)

(shell/sh "convert")

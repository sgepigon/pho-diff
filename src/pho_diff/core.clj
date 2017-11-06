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

(def tagalog-cons "resources/inventory/tagalogipacons.gif")
(def english-cons "resources/inventory/englishipacons.gif")


(conch/with-programs [ls] (ls english-cons))

(conch/programs convert cd ls pwd)

(shell/sh "ls" english-cons)
(shell/sh "convert")

(defn diff
  "Return the diff gif for languages a and b.

  The features only found in language a are colored red, and the features only
  found in language b are colored green. The features common to both languages
  remain in grayscale."
  ([a b out]
   (convert "(" (str b) "-flatten" "-grayscale" "Rec709Luminance" ")"
            "(" (str a) "-flatten" "-grayscale" "Rec709Luminance" ")"
            "(" "-clone" "0-1" "-compose" "darken" "-composite" ")"
            "-channel" "RGB" "-combine" (str out)))
  ([a b]
   (diff a b "diff.gif")))


(diff english-cons tagalog-cons)

(comment (set! spec/*explain-out* expound/printer))

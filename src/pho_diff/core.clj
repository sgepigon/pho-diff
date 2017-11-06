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

(conch/programs convert)

(defn- inventory
  "Return the path of the language's inventory for the given articulation, either
  consonants (cons) or vowels (vowels)."
  [lang articulation]
  (str "resources/inventory/" lang "ipa" articulation ".gif"))

(defn- diff-gif
  "Generate the diff gif for languages a.gif and b.gif.

  The features only found in language a are colored red, and the features only
  found in language b are colored green. The features common to both languages
  remain in grayscale."
  [a b out]
  (convert "(" (str b) "-flatten" "-grayscale" "Rec709Luminance" ")"
           "(" (str a) "-flatten" "-grayscale" "Rec709Luminance" ")"
           "(" "-clone" "0-1" "-compose" "darken" "-composite" ")"
           "-channel" "RGB" "-combine" (str "resources/output/" out)))

(defn- diff
  "Generate the diff gif for languages a and b.

  The features only found in language a are colored red, and the features only
  found in language b are colored green. The features common to both languages
  remain in grayscale."
  ([a b articulation]
   (diff-gif (inventory a articulation)
             (inventory b articulation)
             (str a "-" b "-" articulation ".gif")))
  ([a b]
   (let [articulations ["cons" "vowels"]]
     (map #(diff a b %) articulations))))

(diff "cebuano" "tagalog")

(diff "english" "english")

(comment (set! spec/*explain-out* expound/printer))

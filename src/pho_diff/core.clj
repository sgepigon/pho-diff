(ns pho-diff.core
  (:gen-class)
  (:require [clojure.java.shell :as shell]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as spec.gen]
            [clojure.spec.test.alpha :as spec.test]
            [expound.alpha :as expound]
            [me.raynes.conch :as conch]))

(def articulations ["cons" "vowels"])
(def ^:private inventory-path "resources/inventory/")
(def ^:private output-path "resources/output/")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(conch/programs convert)

(spec/def ::lang string?)               ; TODO add the list of available languages
(spec/def ::articulation (spec/and string? (set articulations)))

(spec/fdef inventory
           :args (spec/cat :lang ::lang :articulation ::articulation)
           :ret string?)

(defn- inventory
  "Return the path of the language's inventory for the given articulation, either
  consonants (cons) or vowels (vowels)."
  [lang articulation]
  (str inventory-path lang "ipa" articulation ".gif"))

(defn- diff-gif
  "Generate the diff gif for languages a.gif and b.gif.

  The features only found in language a are colored red, and the features only
  found in language b are colored green. The features common to both languages
  remain in grayscale."
  [a b out]
  (convert "(" (str b) "-flatten" "-grayscale" "Rec709Luminance" ")"
           "(" (str a) "-flatten" "-grayscale" "Rec709Luminance" ")"
           "(" "-clone" "0-1" "-compose" "darken" "-composite" ")"
           "-channel" "RGB" "-combine" (str output-path out)))

(spec/fdef diff
           :args (spec/cat :a ::lang :b ::lang :articulation (spec/? ::articulation))
           :ret any?)

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
   (map #(diff a b %) articulations)))

(diff "cebuano" "tagalog")

(diff "english" "english")

;; ----- spec dev tools -----

(spec.test/instrument)

(comment (set! spec/*explain-out* spec/explain-printer) ; default
         (set! spec/*explain-out* expound/printer)      ; ...
         (set! spec/*explain-out* (expound/custom-printer {:show-valid-values? true})) ; show valid values

         (spec.test/instrument)
         (spec.test/unstrument)
         :end
         )

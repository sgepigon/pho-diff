(ns pho-diff.diff
  (:gen-class)
  (:require [clojure.java.shell :as shell]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as spec.gen]
            [expound.alpha :as expound]
            [me.raynes.conch :as conch]
            [orchestra.spec.test :as spec.test]
            [spec-provider.provider :as provider]))


(def articulations #{"cons" "vowels"})
(def ^:private inventory-path "resources/inventory/")
(def ^:private output-path "resources/output/")

(conch/programs convert)

(spec/def ::lang string?)               ; TODO add the list of available languages
(spec/def ::articulation (spec/and string? articulations))

(spec/fdef inventory
  :args (spec/cat :lang ::lang :articulation ::articulation)
  :ret string?)

(defn- inventory
  "Return the path of the language's inventory for the given articulation, either
  consonants (cons) or vowels (vowels)."
  [lang articulation]
  (str inventory-path lang "ipa" articulation ".gif"))

(spec/fdef diff-gif
  :args (spec/cat :a string? :b string? :out string?)
  :ret any?)

(defn- diff-gif
  "Generate the diff gif for languages a.gif and b.gif.

  The features only found in language a are colored red, and the features only
  found in language b are colored green. The features common to both languages
  remain in grayscale."
  [a b out]
  (convert "(" b "-flatten" "-grayscale" "Rec709Luminance" ")"
           "(" a "-flatten" "-grayscale" "Rec709Luminance" ")"
           "(" "-clone" "0-1" "-compose" "darken" "-composite" ")"
           "-channel" "RGB" "-combine" (str output-path out)))

(spec/fdef diff
  :args (spec/cat :a ::lang :b ::lang :articulation (spec/? ::articulation))
  :ret any?)

(defn diff
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

(defn diff-charts
  "TODO"
  [a b articulation]
  (diff-gif a b articulation))

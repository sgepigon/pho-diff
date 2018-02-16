(ns pho-diff.diff
  (:gen-class)
  (:require [clojure.spec.alpha :as spec]
            [me.raynes.conch :as conch]
            [pho-diff.util :as util]))

(def articulations #{"cons" "vowels"})

(conch/programs convert)

(spec/def ::articulation (spec/and string? articulations))

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
           "-channel" "RGB" "-combine" out))

(spec/fdef diff-charts
  :args (spec/cat :a :pho-diff.scrape/language :b :pho-diff.scrape/language :articulation (spec/? ::articulation))
  :ret any?)

(defn- diff-charts
  "Generate the diff chart for languages a and b.

  The features only found in language a are colored red, and the features only
  found in language b are colored green. The features common to both languages
  remain in grayscale."
  ([a b articulation]
   (diff-gif (util/pathify a articulation)
             (util/pathify b articulation)
             (util/pathify a b articulation)))
  ([a b]
   (doseq [articulation articulations] (diff-charts a b articulation))))

(spec/fdef diff
  :args (spec/cat :a :pho-diff.scrape/language :b :pho-diff.scrape/language)
  :ret any?)

(defn diff
  "TODO Implement full `diff` with `diff-charts` and `other-sounds`"
  [a b])

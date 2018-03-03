(ns pho-diff.diff
  (:gen-class)
  (:require [clojure.spec.alpha :as spec]
            [me.raynes.conch :as conch]
            [pho-diff.lang :as lang]
            [pho-diff.scrape :as scrape]))

(conch/programs convert)

(spec/fdef diff-gif
  :args (spec/cat :a string? :b string? :out string?)
  :ret any?)

(defn- diff-gif
  "Generate the diff gif for languages a.gif and b.gif.

  The features only found in language `a` are colored red, and the features only
  found in language `b` are colored green. The features common to both languages
  remain in grayscale."
  [a b out]
  (convert "(" b "-flatten" "-grayscale" "Rec709Luminance" ")"
           "(" a "-flatten" "-grayscale" "Rec709Luminance" ")"
           "(" "-clone" "0-1" "-compose" "darken" "-composite" ")"
           "-channel" "RGB" "-combine" out))

(spec/fdef diff-charts
  :args (spec/alt :single-chart (spec/cat :a ::lang/language
                                          :b ::lang/language
                                          :articulation ::lang/articulation)
                  :both-charts (spec/cat :a ::lang/language
                                         :b ::lang/language))
  :ret any?)

(defn- diff-charts
  "Generate the diff chart for languages `a` and `b`.

  The features only found in language `a` are colored red, and the features only
  found in language `b` are colored green. The features common to both languages
  remain in grayscale."
  ([a b articulation]
   (diff-gif (lang/->path a articulation)
             (lang/->path b articulation)
             (lang/->path a b articulation)))
  ([a b]
   (for [articulation lang/articulations]
     (do (diff-charts a b articulation)
         (lang/->path a b articulation)))))

(spec/fdef diff
  :args (spec/cat :a ::lang/language :b ::lang/language)
  :ret any?)

(defn diff
  "TODO Implement full `diff` with `diff-charts` and `other-sounds`."
  [a b]
  ;; Are the charts already downloaded?
  (cond
    (lang/diffed? a b) (for [articulation lang/articulations]
                         (lang/->path a b articulation))
    (and (lang/inventory? a) (lang/inventory? b)) (diff-charts a b)
    :else (do (scrape/slurp-charts a)
              (scrape/slurp-charts b)
              (diff-charts a b))))

(ns pho-diff.diff
  "TODO Add ns docstring"
  (:gen-class)
  (:require [clojure.spec.alpha :as spec]
            [me.raynes.conch :as conch]
            [pho-diff.lang :as lang]
            [pho-diff.scrape :as scrape]))

;; `string?` is just a placeholder for `::gif`. It's not robust enough to
;; generate specs, but it's good enough for my purposes. Might create a proper
;; gif filename generator, but not a priority.
(spec/def ::gif string?)

(conch/programs convert)

(spec/fdef gif
  :args (spec/cat :a ::gif :b ::gif :out ::gif)
  :ret any?)

(defn- gif
  "Generate the diff of gifs `a` and `b`.

  The features only found in language `a` are colored red, and the features only
  found in language `b` are colored green. The features common to both languages
  remain in grayscale."
  [a b out]
  (convert "(" b "-flatten" "-grayscale" "Rec709Luminance" ")"
           "(" a "-flatten" "-grayscale" "Rec709Luminance" ")"
           "(" "-clone" "0-1" "-compose" "darken" "-composite" ")"
           "-channel" "RGB" "-combine" out))

(spec/fdef charts
  :args (spec/alt :single-chart (spec/cat :a ::lang/language
                                          :b ::lang/language
                                          :articulation ::lang/articulation)
                  :both-charts (spec/cat :a ::lang/language
                                         :b ::lang/language))
  :ret (spec/or :single-chart #{""}
                :both-charts ::lang/charts))

(defn- charts
  "Generate the diff of the IPA charts for languages `a` and `b`.

  The features only found in language `a` are colored red, and the features only
  found in language `b` are colored green. The features common to both languages
  remain in grayscale."
  ([a b articulation]
   (gif (lang/->path a articulation)
        (lang/->path b articulation)
        (lang/->path a b articulation)))
  ([a b]
   (do (doseq [articulation lang/articulations] (charts a b articulation))
       (lang/->charts a b))))

(spec/fdef summary
  :args (spec/cat :a ::lang/language
                  :b ::lang/language)
  ;; FIXME spec the return map of `summary`
  :ret (spec/keys :req-un [::lang/charts ::other-sounds ::sources]))

(defn summary
  "diff the `summary` of languages `a` and `b`."
  [a b]
  (let [ma (scrape/summary a)
        mb (scrape/summary b)]
    {:ks {:a a :b b}
     :charts (if (lang/diffed? a b)
               (lang/->charts a b)
               ;; only diff/charts if both languages have charts
               (when (and (:charts ma) (:charts mb)) (charts a b)))
     :other-sounds {:a (:other-sounds ma)
                    :b (:other-sounds mb)}
     :sources {:a (:source ma)
               :b (:source mb)}}))

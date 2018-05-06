(ns pho-diff.diff
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

(spec/fdef diff-gif
  :args (spec/cat :a ::gif :b ::gif :out ::gif)
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
   (do (doseq [articulation lang/articulations]
         (diff-charts a b articulation))
       {:cons (lang/->path a b "cons")
        :vowels (lang/->path a b "vowels")})))

(spec/fdef diff
  :args (spec/cat :a ::lang/language :b ::lang/language)
  :ret any?)

(defn diff
  "TODO Implement full `diff` with `diff-charts` and `other-sounds`."
  [a b]
  ;; Are the charts already downloaded?
  (cond
    (lang/diffed? a b) {:charts {:cons (lang/->path a b "cons")
                                 :vowels (lang/->path a b "vowels")}
                        :other-sounds {:a (:other-sounds (scrape/summary a))
                                       :b (:other-sounds (scrape/summary b))}}
    (and (lang/inventory? a) (lang/inventory? b)) (diff-charts a b)
    :else (when (and (:slurp-charts (scrape/summary a))
                     (:slurp-charts (scrape/summary b)))
            ((diff-charts a b)))))

(spec/fdef summary
  :args (spec/cat :a ::lang/language
                  :b ::lang/language)
  ;; FIXME spec the return map of `summary`
  :ret (spec/nilable (spec/keys :req-un [::charts ::other-sounds])))

(defn summary
  "diff the `summary` of languages `a` and `b`.

  TODO potentially make this the default `diff` algorithm."
  [a b]
  (let [ma (scrape/summary a)
        mb (scrape/summary b)]
    {:keys [:a a :b b]
     :charts (diff-charts a b)
     :other-sounds {:a (:other-sounds ma)
                    :b (:other-sounds mb)}
     :sources {:a (:source ma)
               :b (:source mb)}}))

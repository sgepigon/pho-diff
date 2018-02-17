(ns pho-diff.scrape
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as enlive]
            [pho-diff.lang :as lang]
            [pho-diff.util :as util]))

(spec/def ::html-data seq?)

(def ^:private archive-url "http://accent.gmu.edu/browse_native.php")
(def ^:private base-url "http://accent.gmu.edu/browse_native.php?function=detail&languageid=")

(def ^:private archive-data (util/fetch-url archive-url))
(def ^:private lang-data
  (let [languages (enlive/select archive-data [:div#maincontent :ul :li :a])
        name :content
        id #(re-find #"\d+" (first (enlive/attr-values % :href)))]
    (zipmap (mapcat name languages) (map id languages))))

(def ^:private languages (sort (keys lang-data)))
(spec/def ::lang/language (set languages))

(defn- fetch-language
  "TODO Grab the contents of the language specified"
  [language]
  (util/fetch-url (str base-url (get lang-data language))))

(spec/fdef fetch-charts
  :args (spec/cat :html-data ::html-data)
  :ret (spec/coll-of string? :count 2))

(defn- fetch-charts
  "Return the URLs of the IPA charts"
  [html-data]
  (map (comp :src :attrs) (enlive/select html-data [:div.content :p :img])))

(spec/fdef slurp-charts
  :args (spec/cat :language ::lang/language)
  ;; TODO Do I need to write `spec/def`s for ::cons and ::vowels?
  :ret (spec/keys :req-un [::cons ::vowels]))

(defn slurp-charts
  "Return a map of consonant and vowel charts for the input language

  Source: https://stackoverflow.com/questions/11321264/"
  [language]
  (let [[cons-uri vowels-uri] (fetch-charts (fetch-language language))
        cons-path (util/->path language "cons")
        vowels-path (util/->path language "vowels")]
    (do (util/copy-uri-to-file cons-uri cons-path) ; TODO is this idiomatic use of `do`?
        (util/copy-uri-to-file vowels-uri vowels-path)
        {:cons cons-path
         :vowels vowels-path})))

(defn- other-sounds-str
  "Helper function for `other-sounds`

  TODO A bit hard-coded and ugly, but it works. Would like to parse in idiomatic
  Enlive."
  [html-data]
  (-> (enlive/select html-data [:div.content])
      first
      :content
      (nth 4)))

(spec/fdef other-sounds
  :args (spec/cat :html-data ::html-data)
  :ret (spec/coll-of string? :kind set?))

(defn- other-sounds
  "Return a set of the phonetic features not included on the IPA chart."
  [html-data]
  (set (map string/triml (-> (other-sounds-str html-data)
                             (string/replace #"other sounds:" "")
                             (string/replace #"\." "")
                             (string/split #";")))))

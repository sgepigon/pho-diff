(ns pho-diff.scrape
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as spec.gen]
            [clojure.string :as string]
            [expound.alpha :as expound]
            [me.raynes.conch :as conch]
            [net.cgrand.enlive-html :as enlive]
            [orchestra.spec.test :as spec.test]
            [spec-provider.provider :as provider]))

(spec/def ::html-data seq?)

(def ^:private inventory-path "resources/inventory/")
(def ^:private archive-url "http://accent.gmu.edu/browse_native.php")
(def ^:private base-url "http://accent.gmu.edu/browse_native.php?function=detail&languageid=")

(defn- fetch-url
  "Grab the contents of the URL specified"
  [url]
  (enlive/html-resource (java.net.URL. url)))

(defn- copy-uri-to-file
  "Copy a URI to file

  Source: https://stackoverflow.com/questions/15628682/"
  [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(def ^:private archive-data (fetch-url archive-url))
(def ^:private lang-data
  (let [languages (enlive/select archive-data [:div#maincontent :ul :li :a])
        name :content
        id #(re-find #"\d+" (first (enlive/attr-values % :href)))]
    (zipmap (mapcat name languages) (map id languages))))

(def ^:private languages (sort (keys lang-data)))
(spec/def ::language (set languages))

(defn- language->kebab
  "Conform any language string to kebab-style"
  [s]
  (string/replace s #"\s" "-"))

(defn pathify
  "Create a file path for a language and articulation"
  [language articulation]
  (str inventory-path (language->kebab language) "ipa" articulation ".gif"))

(defn- fetch-language
  "TODO Grab the contents of the language specified"
  [language]
  (fetch-url (str base-url (get lang-data language))))

(spec/fdef fetch-charts
  :args (spec/cat :html-data ::html-data)
  :ret (spec/coll-of string? :count 2))

(defn- fetch-charts
  "Return the URLs of the IPA charts"
  [html-data]
  (map (comp :src :attrs) (enlive/select html-data [:div.content :p :img])))

(spec/fdef slurp-charts
  :args (spec/cat :language ::language)
  ;; TODO Do I need to write `spec/def`s for ::cons and ::vowels?
  :ret (spec/keys :req-un [::cons ::vowels]))

(defn slurp-charts
  "Return a map of consonant and vowel charts for the input language

  Source: https://stackoverflow.com/questions/11321264/"
  [language]
  (let [[cons-uri vowels-uri] (fetch-charts (fetch-language language))
        cons-path (pathify language "cons")
        vowels-path (pathify language "vowels")]
    (do (copy-uri-to-file cons-uri cons-path) ; TODO is this idiomatic use of `do`?
        (copy-uri-to-file vowels-uri vowels-path)
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

(ns pho-diff.scrape
  (:require [clojure.spec.alpha :as spec]
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

(def ^:private archive-data (fetch-url archive-url))
(def ^:private lang-data
  (let [languages (enlive/select archive-data [:div#maincontent :ul :li :a])
        name :content
        id #(re-find #"\d+" (first (enlive/attr-values % :href)))]
    (zipmap (mapcat name languages) (map id languages))))

(defn- fetch-language
  "TODO Grab the contents of the language specified"
  [language]
  (fetch-url (str base-url (get lang-data language))))

(spec/fdef fetch-charts
  :args (spec/cat :html-data ::html-data)
  :ret (spec/coll-of string?))

(defn- fetch-charts
  "Return the URLs of the IPA charts"
  [html-data]
  (map (comp :src :attrs) (enlive/select html-data [:div.content :p :img])))

(spec/fdef slurp-charts
  :args (spec/cat :language-name string?)
  :ret map?)

(defn copy-uri-to-file [uri file]
  "TODO https://stackoverflow.com/questions/15628682/in-clojure-how-do-you-download-an-image-from-the-web-and-save-it-to-your-file-s"
  (with-open [in (clojure.java.io/input-stream uri)
              out (clojure.java.io/output-stream file)]
    (clojure.java.io/copy in out)))

(defn slurp-charts
  "FIXME docstring
  https://stackoverflow.com/questions/11321264/saving-an-image-form-clj-http-request-to-file"
  [language]
  (let [[cons-uri vowels-uri] (fetch-charts (fetch-language language))
        cons-path (str inventory-path language "ipacons.gif")
        vowels-path (str inventory-path language "ipavowels.gif")]
    (do (copy-uri-to-file cons-uri cons-path)
        (copy-uri-to-file vowels-uri vowels-path)
        {:cons cons-path
         :vowels vowels-path})))

(spec/fdef language-name
  :args (spec/cat :html-data ::html-data)
  :ret string?)

(defn- language-name
  "Return the string of the language

  TODO: parsing the content is still a bit hard-coded. Idiomatic Enlive for this
  would be nice."
  [html-data]
  (-> (enlive/select html-data [:div.content :h5])
      first
      :content
      first
      (string/replace "Native Phonetic Inventory:" "")
      string/trim))

(defn- other-sounds-str
  "TODO A bit hard-coded and ugly, but it works. Would like to parse in idiomatic
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

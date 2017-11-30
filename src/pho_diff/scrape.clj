(ns pho-diff.scrape
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as spec.gen]
            [clojure.spec.test.alpha :as spec.test]
            [clojure.string :as string]
            [expound.alpha :as expound]
            [net.cgrand.enlive-html :as enlive]))

(def archive-url "http://accent.gmu.edu/browse_native.php")
(def base-url "http://accent.gmu.edu/browse_native.php?function=detail&languageid=")


(defn fetch-url
  "Grab the contents of the URL specified"
  [url]
  (enlive/html-resource (java.net.URL. url)))


(defn names->ids                      ; TODO get a better name
  "TODO"
  [html-data]
  (let [languages (enlive/select html-data [:div#maincontent :ul :li :a])
        name :content
        id #(re-find #"\d+" (first (enlive/attr-values % :href)))]
    (zipmap (mapcat name languages) (map id languages))))


(def archive-data (fetch-url archive-url))
(def lang-data (names->ids archive-data)) ;; TODO


(defn fetch-language
  "TODO Grab the contents of the language specified"
  [language]
  (fetch-url (str base-url (get lang-data language))))

(spec/def ::html-data seq?)

(defn- fetch-gif-urls
  "Return the URLs of the IPA charts"
  [html-data]
  (map (comp :src :attrs) (enlive/select html-data [:div.content :p :img])))


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
      string/triml))


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

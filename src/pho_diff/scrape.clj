(ns pho-diff.scrape
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as spec.gen]
            [clojure.spec.test.alpha :as spec.test]
            [clojure.string :as string]
            [expound.alpha :as expound]
            [net.cgrand.enlive-html :as enlive]))

(def archive-url "http://accent.gmu.edu/browse_native.php?function=detail&languageid=38")

(defn fetch-url
  "Grab the contents of the url specified"
  [url]
  (enlive/html-resource (java.net.URL. url)))

(def html-data (fetch-url archive-url))

(defn- fetch-gif-urls
  [html-data]
  (map (comp :src :attrs) (enlive/select html-data [:div.content :p :img])))

(fetch-gif-urls html-data)

(spec/fdef language-name
  :args (spec/cat :html-data seq?)
  :ret string?)

(defn- language-name
  "Return the string of the language

  TODO: parsing the content is still a bit hardcoded. Idomatic Enlive for this
  would be nice."
  [html-data]
  (-> (enlive/select html-data [:div.content :h5])
      first
      :content
      first
      (string/replace "Native Phonetic Inventory:" "")
      string/triml))

(defn- other-sounds-str
  "TODO A bit hardcoded and ugly, but it works. Would like to parse in idomatic
  Enlive."
  [html-data]
  (-> (enlive/select html-data [:div.content])
      first
      :content
      (nth 4)))

(spec/fdef other-sounds
  :args (spec/cat :html-data seq?)
  :ret (spec/coll-of string? :kind set?))

(defn- other-sounds
  "Return a set of the phonetic features not included on the IPA chart."
  [html-data]
  (set (map string/triml (-> (other-sounds-str html-data)
                             (string/replace #"other sounds:" "")
                             (string/replace #"\." "")
                             (string/split #";")))))

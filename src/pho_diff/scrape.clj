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


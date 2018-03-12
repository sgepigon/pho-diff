(ns pho-diff.scrape
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as enlive]
            [pho-diff.lang :as lang]))

(spec/def ::html-data seq?)

(def ^:private archive-url "http://accent.gmu.edu/browse_native.php")
(def ^:private base-url "http://accent.gmu.edu/browse_native.php?function=detail&languageid=")

(defn fetch-url
  "Grab the contents of the `URL` specified."
  [url]
  (enlive/html-resource (java.net.URL. url)))

(defn copy-uri-to-file
  "Copy a `URI` to `file`.

  Source: https://stackoverflow.com/questions/15628682/"
  [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(comment
  (def ^:private archive-data (fetch-url archive-url))
  (def ^:private lang-data
    (let [languages (enlive/select archive-data [:div#maincontent :ul :li :a])
          name :content
          id #(re-find #"\d+" (first (enlive/attr-values % :href)))]
      (zipmap (mapcat name languages) (map id languages))))

  (def ^:private languages (sort (keys lang-data))))

(defn- fetch-language
  "Grab the HTML contents of `language`."
  [language]
  (fetch-url (str base-url (get lang/ids language))))

(spec/fdef fetch-charts
  :args (spec/cat :html-data ::html-data)
  :ret (spec/keys :req-un [::cons ::vowels]))

(defn- fetch-charts
  "Return a map of the IPA charts URLs.

  Returns `nil` if the charts are not found."
  [html-data]
  (let [[cons vowels] (map (comp :src :attrs)
                           (enlive/select html-data [:div.content :p :img]))]
    (when (and cons vowels)
      {:cons cons
       :vowels vowels})))

(spec/fdef slurp-charts
  :args (spec/cat :language ::lang/language)
  :ret (spec/keys :req-un [::cons ::vowels]))

(defn slurp-charts
  "Return a map of consonant and vowel charts for `language`.

  Returns `nil` if the charts are not found."
  [language]
  (when-let [m (fetch-charts (fetch-language language))]
    (let [cons-path (lang/->path language "cons")
          vowels-path (lang/->path language "vowels")]
      (do (copy-uri-to-file (:cons m) cons-path)
          (copy-uri-to-file (:vowels m) vowels-path)
          {:cons cons-path
           :vowels vowels-path}))))

(defn- other-sounds-str
  "Helper function for `other-sounds`."
  [language]
  ;; TODO A bit hard-coded and ugly, but it works. Would like to parse in
  ;; idiomatic Enlive.
  (-> (fetch-language language) (enlive/select [:div.content]) first :content (nth 4 nil)))

(spec/fdef other-sounds
  :args (spec/cat :language ::lang/language)
  :ret (spec/coll-of string? :kind set?))

(defn other-sounds
  "Return a map containing a set of the phonetic features not included on the IPA
  chart.

  Returns `nil` if the phonetic features are not found."
  [language]
  (when-let [s (other-sounds-str language)]
    {:other-sounds (->> (-> s
                            (string/replace #"other sounds:" "")
                            (string/replace #"\." "")
                            (string/split #";"))
                        (map string/triml)
                        set)}))

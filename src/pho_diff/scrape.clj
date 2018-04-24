(ns pho-diff.scrape
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as enlive]
            [pho-diff.lang :as lang]))

;; `seq?` is just a placeholder for `::html-data`. It's not robust enough to
;; generate specs, but it's good enough for my purposes.
(spec/def ::html-data seq?)

(def ^:private archive-url "http://accent.gmu.edu/browse_native.php")
(def ^:private base-url "http://accent.gmu.edu/browse_native.php?function=detail&languageid=")

(defn- fetch-url
  "Grab the contents of the `URL` specified."
  [url]
  (enlive/html-resource (java.net.URL. url)))

(defn- copy-uri-to-file
  "Copy a `URI` to `file`.

  Source: https://stackoverflow.com/questions/15628682/"
  [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(comment
  (when-let [archive (fetch-url archive-url)
             languages (enlive/select archive [:div#maincontent :ul :li :a])
             name :content
             id #(re-find #"\d+" (first (enlive/attr-values % :href)))
             names (mapcat name languages)
             name->id (zipmap names (map id languages))]
    (binding [*print-length* false]     ; print the full map or set
      (do (spit "resources/inventory/ids.edn" (pr-str name->id))
          (spit "resources/inventory/languages.edn" (pr-str (set names)))))))

(defn- fetch-language!
  "Grab the HTML contents of `language`."
  [language]
  (fetch-url (str base-url (get lang/ids language))))

(spec/fdef fetch-charts
  :args (spec/cat :html-data ::html-data)
  :ret (spec/nilable (spec/keys :req-un [::cons ::vowels])))

(defn- fetch-charts
  "Return a map of the IPA charts URLs.

  Returns `nil` if the charts are not found."
  [html-data]
  (when-let [[cons vowels] (-> html-data
                               (enlive/select [:div.content :p :img])
                               (->> (mapv (comp :src :attrs))))]
    {:cons cons :vowels vowels}))

(spec/fdef slurp-charts
  :args (spec/cat :language ::lang/language
                  :html-data ::html-data)
  :ret (spec/nilable (spec/keys :req-un [::cons ::vowels])))

(defn- slurp-charts
  "Return a map of consonant and vowel charts for `language`.

  Returns `nil` if the charts are not found."
  [language html-data]
  (when-let [{:keys [cons vowels]} (fetch-charts html-data)]
    (let [cons-path (lang/->path language "cons")
          vowels-path (lang/->path language "vowels")]
      (do (copy-uri-to-file cons cons-path)
          (copy-uri-to-file vowels vowels-path)
          {:cons cons-path
           :vowels vowels-path}))))

(spec/fdef other-sounds-str
  :args (spec/cat :html-data ::html-data)
  :ret (spec/nilable string?))

(defn- other-sounds-str
  "Helper function for `other-sounds`."
  [html-data]
  ;; TODO A bit hard-coded and ugly, but it works. Would like to parse in
  ;; idiomatic Enlive. Maybe a zipper could work here...
  (some-> html-data
          (enlive/select [:div.content])
          first
          :content
          (nth 4 nil)
          ;; Return iff string?. e.g. "greek" returns a map so return `nil` instead.
          (#(when (string? %) %))))

(spec/fdef other-sounds
  :args (spec/cat :html-data ::html-data)
  :ret (spec/nilable (spec/keys :req-un [::lang/other-sounds])))

(defn- other-sounds
  "Return a map containing a set of the phonetic features not included on the IPA
  chart.

  Returns `nil` if the phonetic features are not found."
  [html-data]
  (some-> (other-sounds-str html-data)
          (string/replace #"other sounds:" "")
          (string/replace #"\." "")
          (string/split #";")
          (->> (map string/triml)
               set
               (assoc {} :other-sounds))))

(spec/fdef summary
  :args (spec/cat :language ::lang/language)
  :ret map?)                            ; FIXME spec the return map of `summary`

(defn summary
  "TODO Return a map with all the information about the `langauge`.

  TODO See if I can let the `fetch-language!` be a local var so I can save time
  and space."
  [language]
  (when-let [html-data (fetch-language! language)]
    (merge {:charts (slurp-charts language html-data)}
           (other-sounds html-data))))

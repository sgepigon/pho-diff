(ns pho-diff.scrape
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as enlive]
            [pho-diff.lang :as lang]))

;; `seq?` is just a placeholder for `::html-data`. It's not robust enough to
;; generate specs, but it's good enough for my purposes.
(spec/def ::html-data seq?)

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

(defn inventory!
  "Scrape the language names and name->id map and write to files to `path`."
  [path]
  (let [archive (fetch-url "http://accent.gmu.edu/browse_native.php")
        languages (enlive/select archive [:div#maincontent :ul :li :a])
        name :content
        id #(re-find #"\d+" (first (enlive/attr-values % :href)))
        names (mapcat name languages)
        name->id (zipmap names (map id languages))]
    ;; print the full map or set to file
    (binding [*print-length* false]
      (do (spit (io/file path "languages.edn") (pr-str (set names)))
          (spit (io/file path "ids.edn") (pr-str name->id))))))

(spec/fdef fetch-charts
  :args (spec/cat :html-data ::html-data)
  :ret (spec/nilable (spec/keys :req-un [::cons ::vowels])))

(defn- fetch-charts
  "Return a map of the IPA charts URLs.

  Returns `nil` if the charts are not found."
  [html-data]
  (when-let [[cons vowels] (-> html-data
                               (enlive/select [:div.content :p :img])
                               (->> (mapv (comp :src :attrs)))
                               seq)]
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
  :ret (spec/nilable ::lang/other-sounds))

(defn- other-sounds
  "Return the set of the phonetic features not included on the IPA chart.

  Returns `nil` if the phonetic features are not found."
  [html-data]
  (some-> (other-sounds-str html-data)
          (string/replace #"other sounds:" "")
          (string/replace #"\." "")
          (string/split #";")
          (->> (map string/triml)
               set)))

(spec/fdef summary
  :args (spec/cat :language ::lang/language)
  ;; FIXME spec the return map of `summary`
  :ret (spec/nilable (spec/keys :req-un [::charts ::other-sounds ::source])))

(defn summary
  "TODO Return a map with all the information about the `langauge`."
  [language]
  (when-let [html-data (->> language lang/->url fetch-url)]
    {:charts (slurp-charts language html-data)
     :other-sounds (other-sounds html-data)
     :source (lang/->url language)}))

(ns pho-diff.lang
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.string :as string]))

(def ^:private inventory "resources/inventory/")
(def ^:private output "resources/output/")

(def articulations #{"cons" "vowels"})
(def languages (edn/read-string (slurp (str inventory "languages.edn"))))
(def ids (edn/read-string (slurp (str inventory "ids.edn"))))

(spec/def ::articulation articulations)
(spec/def ::language languages)
(spec/def ::other-sounds (spec/coll-of string? :kind set?))
(spec/def ::charts (spec/nilable (spec/keys :req-un [::cons ::vowels])))

(spec/fdef ->url
  :args (spec/cat :language ::language)
  :ret string?)

(defn ->url
  "Return the URL of the language in the Speech Accent Archive."
  [language]
  (let [base "http://accent.gmu.edu/browse_native.php?function=detail&languageid="]
    (str base (get ids language))))

(spec/fdef ->kebab
  :args (spec/cat :s string?)
  :ret string?)

(defn ->kebab
  "Replace invalid filename characters with dashes in a string `s`, kebab-style."
  [s]
  ;; regex source: https://stackoverflow.com/a/2059612
  (let [invalid-chars #"[^a-zA-Z0-9\\-\\.]+"]
    (-> s string/lower-case string/trim (string/replace invalid-chars "-"))))

(spec/fdef ->filename
  :args (spec/alt :single (spec/cat :language ::language
                                    :articulation ::articulation)
                  :pair (spec/cat :a ::language
                                  :b ::language
                                  :articulation ::articulation))
  :ret string?)

(defn ->filename
  "Create a file name for a `language` inventory or for a diff of two
  inventories,`a` and `b`."
  ([language articulation]
   (str (->kebab language) "ipa" articulation ".gif"))
  ([a b articulation]
   (str (->kebab a) "-" (->kebab b) "-" articulation ".gif")))

(spec/fdef ->path
  :args (spec/alt :single (spec/cat :language ::language
                                    :articulation ::articulation)
                  :pair (spec/cat :a ::language
                                  :b ::language
                                  :articulation ::articulation))
  :ret string?)

(defn ->path
  "Create a file path for a `language` inventory or for a diff of two
  inventories,`a` and `b`."
  ([language articulation]
   (str inventory (->filename language articulation)))
  ([a b articulation]
   (str output (->filename a b articulation))))

(spec/fdef ->charts
  :args (spec/cat :a ::language :b ::language)
  :ret ::charts)

(defn ->charts
  "Return a map of the file paths of the consonant and vowel diff of languages
  `a`and`b`."
  [a b]
  {:cons (->path a b "cons")
   :vowels (->path a b "vowels")})

(spec/fdef inventory?
  :args (spec/cat :language ::language)
  :ret boolean?)

(defn inventory?
  "Check if the charts for `language` are already in the inventory."
  [language]
  (every? true? (for [articulation articulations]
                  (.exists (io/file (->path language articulation))))))

(spec/fdef diffed?
  :args (spec/cat :a ::language
                  :b ::language)
  :ret boolean?)

(defn diffed?
  "Check if two languages `a` and `b` have already been diffed."
  [a b]
  (every? true? (for [articulation articulations]
                  (.exists (io/file (->path a b articulation))))))

(ns pho-diff.lang
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.string :as string]))

(def ^:private inventory "resources/inventory/")
(def ^:private output "resources/output/")

(def articulations #{"cons" "vowels"})
(spec/def ::articulation articulations)

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

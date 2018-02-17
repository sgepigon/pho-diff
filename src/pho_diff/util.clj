(ns pho-diff.util
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as enlive]
            [pho-diff.lang :as lang]))

(def ^:private inventory "resources/inventory/")
(def ^:private output "resources/output/")

(defn fetch-url
  "Grab the contents of the URL specified"
  [url]
  (enlive/html-resource (java.net.URL. url)))

(defn copy-uri-to-file
  "Copy a URI to file

  Source: https://stackoverflow.com/questions/15628682/"
  [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(spec/fdef whitespace->kebab
  :args (spec/cat :s string?)
  :ret string?)

(defn whitespace->kebab
  "Replace internal whitespaces with dashes in a string kebab-style"
  [s]
  (-> s string/trim (string/replace #"\s" "-")))

(spec/fdef ->filename
  :args (spec/alt :single (spec/cat :language ::lang/language
                                    :articulation ::lang/articulation)
                  :pair (spec/cat :a ::lang/language
                                  :b ::lang/language
                                  :articulation ::lang/articulation))
  :ret string?)

(defn ->filename
  "Create a file name for a language inventory or a diff of two inventories, a b"
  ([language articulation]
   (str (whitespace->kebab language) "ipa" articulation ".gif"))
  ([a b articulation]
   (str (whitespace->kebab a) "-" (whitespace->kebab b) "-" articulation ".gif")))

(spec/fdef ->path
  :args (spec/alt :single (spec/cat :language ::lang/language
                                    :articulation ::lang/articulation)
                  :pair (spec/cat :a ::lang/language
                                  :b ::lang/language
                                  :articulation ::lang/articulation))
  :ret string?)

(defn ->path
  "Create a file path for a language inventory or an diff of two inventories, a b"
  ([language articulation]
   (str inventory (->filename language articulation)))
  ([a b articulation]
   (str output (->filename a b articulation))))

(spec/fdef inventory?
  :args (spec/cat :language ::lang/language)
  :ret boolean?)

(defn inventory?
  "Check if the charts for a given language are already in the inventory"
  [language]
  (every? true? (for [articulation lang/articulations]
                  (.exists (io/file (->path language articulation))))))

(spec/fdef diffed?
  :args (spec/cat :a ::lang/language
                  :b ::lang/language)
  :ret boolean?)

(defn diffed?
  "Check if two languages have already been diffed"
  [a b]
  (every? true? (for [articulation lang/articulations]
                  (.exists (io/file (->path a b articulation))))))

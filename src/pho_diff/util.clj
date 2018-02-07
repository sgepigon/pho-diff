(ns pho-diff.util
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as spec.gen]
            [clojure.string :as string]
            [expound.alpha :as expound]
            [me.raynes.conch :as conch]
            [net.cgrand.enlive-html :as enlive]
            [orchestra.spec.test :as spec.test]
            [spec-provider.provider :as provider]))

(def ^:private inventory-path "resources/inventory/")
(def ^:private output-path "resources/output/")

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

(defn pathify
  "Create a file path for a language inventory or an diff of two inventories, a b"
  ([language articulation]
   (str inventory-path (whitespace->kebab language) "ipa" articulation ".gif"))
  ([a b articulation]
   (str output-path
        (whitespace->kebab a) "-" (whitespace->kebab b) "-"
        articulation ".gif")))

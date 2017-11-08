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

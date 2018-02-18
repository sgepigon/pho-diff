(ns pho-diff.lang
  (:require [clojure.spec.alpha :as spec]))

(def articulations #{"cons" "vowels"})
(spec/def ::articulation articulations)

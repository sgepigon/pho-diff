(ns pho-diff.core
  (:gen-class)
  (:require [clojure.java.browse :as browse]
            [clojure.pprint :as pprint]
            [pho-diff.diff :as diff]))

(defn -main
  "I don't do a whole lot ... yet."
  [a b & args]
  (let [summary (diff/summary a b)]
    (do (browse/browse-url (-> summary :charts :cons))
        (browse/browse-url (-> summary :charts :vowels))
        (pprint/pprint summary))))

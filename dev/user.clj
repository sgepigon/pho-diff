(ns user
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as spec.gen]
            [clojure.spec.test.alpha :as spec.test]
            [expound.alpha :as expound]
            [orchestra.spec.test :as orchestra]
            [pho-diff.core :refer :all]
            [pho-diff.diff :as diff]
            [pho-diff.lang :as lang]
            [pho-diff.scrape :as scrape]))

(defn -main []
  (do
    (set! spec/*explain-out* (expound/custom-printer {:print-specs? false
                                                      :show-valid-values? true
                                                      :theme :figwheel-theme}))
    (orchestra/instrument)))

(defn check-result
  "Return the results of `spec.test/check` on `sym` given `num-tests` (default
  1000).

  See `spec.test/check` for options and return. Inspired by/lifted from
  https://clojureverse.org/t/1448,
  https://stackoverflow.com/questions/40697841/, and
  https://dev.clojure.org/jira/browse/CLJ-2246."
  ([sym num-tests]
   (let [check-opts {:clojure.spec.test.check/opts {:num-tests num-tests}}]
     (-> (spec.test/check sym check-opts) first :clojure.spec.test.check/ret)))
  ([sym]
   (-> (spec.test/check sym) first :clojure.spec.test.check/ret)))

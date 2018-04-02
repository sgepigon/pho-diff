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

(orchestra/instrument)

;; https://oli.me.uk/2017/10/18/taming-clojure-spec-with-expound/
(alter-var-root #'spec/*explain-out* (constantly (expound/custom-printer
                                                  {:show-valid-values? true})))

(defn restrument
  "Re-instrument all functions

  Hooked into `cider-refresh`."
  []
  (orchestra/instrument))

(defn check-result
  "Return the results of `spec.test/check` on `sym-or-syms`

  TODO The result is currently nested too deep and `spec.test/abbrev-result` is
  not working, see CLJ-2246 for more details:
  https://dev.clojure.org/jira/browse/CLJ-2246"
  [sym-or-syms]
  (-> (spec.test/check sym-or-syms) first second second))

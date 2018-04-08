(ns pho-diff.scrape-test
  (:require [clojure.test :refer :all]
            [pho-diff.scrape :as scrape]))

(deftest other-sounds-test
  (testing "English has a non-`nil` other sounds."
    (is (= (scrape/other-sounds "english")
           {:other-sounds #{"labio-velar voiced central approximant [w]"
                            "5 diphthongs"}})))
  (testing "\"greek\" returns `nil` (earlier implementations incorrectly
  returned a map)."
    (is (nil? (scrape/other-sounds "greek")))))

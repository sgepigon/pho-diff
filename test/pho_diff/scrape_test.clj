(ns pho-diff.scrape-test
  (:require [clojure.spec.gen.alpha :as spec.gen]
            [clojure.spec.test.alpha :as spec.test]
            [clojure.test :refer :all]
            [pho-diff.scrape :as scrape]
            [user :as user]))

(deftest other-sounds-test
  (testing "English has a non-`nil` other sounds."
    (is (= (scrape/other-sounds "english")
           {:other-sounds #{"labio-velar voiced central approximant [w]"
                            "5 diphthongs"}})))
  (testing "\"greek\" returns `nil` (earlier implementations incorrectly
  returned a map)."
    (is (nil? (scrape/other-sounds "greek"))))
  (testing "Generative testing via spec. There are 369 languages."
    (is (nil? (:false (user/check-result `scrape/other-sounds 369))))))

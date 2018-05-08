(ns pho-diff.scrape-test
  (:require [clojure.spec.gen.alpha :as spec.gen]
            [clojure.spec.test.alpha :as spec.test]
            [clojure.test :refer :all]
            [expound.alpha :as expound]
            [pho-diff.scrape :as scrape]
            [user :as user]))

(deftest corner-cases-test
  ;; Famous values and common corner-cases via
  ;; https://lispcast.com/unit-testing-in-functional-languages/
  (testing "Empty string."
    (is (thrown? clojure.lang.ExceptionInfo (scrape/summary ""))))
  (testing "Not that kind of language..."
    (is (thrown? clojure.lang.ExceptionInfo (scrape/summary "Clojure"))))
  (testing "Languages are not capitalized."
    (is (thrown? clojure.lang.ExceptionInfo (scrape/summary "English"))))
  (testing "Normal case."
    (is (= (scrape/summary "english")
           {:charts {:cons "resources/inventory/englishipacons.gif",
                     :vowels "resources/inventory/englishipavowels.gif"},
            :other-sounds #{"labio-velar voiced central approximant [w]" "5 diphthongs"},
            :source "http://accent.gmu.edu/browse_native.php?function=detail&languageid=18"}))))

(deftest other-sounds-test
  (testing "English has a non-`nil` other sounds."
    (is (= (:other-sounds (scrape/summary "english"))
           #{"labio-velar voiced central approximant [w]"
             "5 diphthongs"})))
  (testing "\"greek\" returns `nil` (earlier implementations incorrectly
  returned a map)."
    (is (nil? (:other-sounds (scrape/summary "greek"))))))

(deftest summary-generative-test
  (testing "Generative testing via spec. There are 369 languages."
    (let [{:keys [result result-data fail shrunk]} (user/check-result `scrape/summary 369)]
      (is result (str {:fail fail
                       :shrunk (:smallest shrunk)
                       :result-data result-data})))))


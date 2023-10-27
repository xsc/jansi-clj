(ns jansi-clj.core-test
  (:require [jansi-clj.core :as jansi]
            [clojure.test :refer [deftest testing is are]]))

(deftest test-color-list
  (is (every? keyword? (jansi/colors))))

(deftest test-attribute-list
  (is (every? keyword? (jansi/attributes))))

(deftest print-formatting
  (testing "printing out escape sequences."
    (let [s "hello"]
      (doseq [c (jansi/colors)]
        (doseq [[f t] [[jansi/fg "fg"]
                       [jansi/bg "bg"]
                       [jansi/fg-bright "fg-bright"]
                       [jansi/bg-bright "bg-bright"]]]
          (printf "%-11s %-8s %s%n" (str "[" t "]") c (f c s)))))))

(deftest test-formatting
  (testing "generic colorization functions"
    (let [s "hello"]
      (doseq [c (jansi/colors)]
        (is (not= (jansi/fg c s) s))
        (is (not= (jansi/fg-bright c s) s))
        (is (not= (jansi/bg c s) s))
        (is (not= (jansi/bg-bright c s) s))
        (jansi/disable!)
        (is (= (jansi/fg c s) s))
        (is (= (jansi/fg-bright c s) s))
        (is (= (jansi/bg c s) s))
        (is (= (jansi/bg-bright c s) s))
        (jansi/enable!))
      (let [colorized-string (jansi/fg :black s s)]
        (is (= (count (re-seq (re-pattern s) colorized-string)) 2)))
      (jansi/disable!)
      (is (= (jansi/fg :black s s) (str s s)))
      (jansi/enable!)))
  (testing "generic attribute function"
    (let [s "hello"]
      (doseq [attr (jansi/attributes)]
        (is (not= (jansi/a attr s) s))
        (jansi/disable!)
        (is (= (jansi/a attr s) s))
        (jansi/enable!)))))

(deftest test-generated-functions
  (testing "generated colorization functions"
    (doseq [c (jansi/colors)]
      (are [suffix] (let [v (resolve (symbol "jansi-clj.core" (str (name c) suffix)))]
                      (and (var? v) (fn? (var-get v))))
           ""
           "-bg"
           "-bright"
           "-bg-bright")))
  (testing "generated formatting functions"
    (doseq [attr (jansi/attributes)]
      (let [v (resolve (symbol "jansi-clj.core" (name attr)))]
        (is (var? v))
        (is (fn? (var-get v)))))))

(deftest t-cursor-functions
  (doseq [[save restore] [[jansi/save-cursor jansi/restore-cursor]
                          [jansi/save-cursor-dec jansi/restore-cursor-dec]
                          [jansi/save-cursor-sco jansi/restore-cursor-sco]]]
    (is (save))
    (is (jansi/cursor-down 5))
    (is (jansi/cursor-right 5))
    (is (jansi/erase-line))
    (is (jansi/cursor-left 1))
    (is (jansi/cursor-up 1))
    (is (restore)))
  (is (jansi/erase-screen)))

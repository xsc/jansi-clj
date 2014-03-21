(ns jansi-clj.core-test
  (:require [jansi-clj.core :refer :all]
            [clojure.test :refer :all]))

(deftest test-color-list
  (is (every? keyword? (colors))))

(deftest test-attribute-list
  (is (every? keyword? (attributes))))

(deftest print-formatting
  (testing "printing out escape sequences."
    (let [s "hello"]
      (doseq [c (colors)]
        (doseq [[f t] [[fg "fg"] [bg "bg"] [fg-bright "fg-bright"] [bg-bright "bg-bright"]]]
          (printf "%-11s %-8s %s%n" (str "[" t "]") c (f c s)))))))

(deftest test-formatting
  (testing "generic colorization functions"
    (let [s "hello"]
      (doseq [c (colors)]
        (is (not= (fg c s) s))
        (is (not= (fg-bright c s) s))
        (is (not= (bg c s) s))
        (is (not= (bg-bright c s) s))
        (disable!)
        (is (= (fg c s) s))
        (is (= (fg-bright c s) s))
        (is (= (bg c s) s))
        (is (= (bg-bright c s) s))
        (enable!))
      (let [colorized-string (fg :black s s)]
        (is (= (count (re-seq (re-pattern s) colorized-string)) 2)))
      (disable!)
      (is (= (fg :black s s) (str s s)))
      (enable!)))
  (testing "generic attribute function"
    (let [s "hello"]
      (doseq [attr (attributes)]
        (is (not= (a attr s) s))
        (disable!)
        (is (= (a attr s) s))
        (enable!)))))

(deftest test-generated-functions
  (testing "generated colorization functions"
    (doseq [c (colors)]
      (are [suffix] (let [v (resolve (symbol "jansi-clj.core" (str (name c) suffix)))]
                      (and (var? v) (fn? (var-get v))))
           ""
           "-bg"
           "-bright"
           "-bg-bright")))
  (testing "generated formatting functions"
    (doseq [attr (attributes)]
      (let [v (resolve (symbol "jansi-clj.core" (name attr)))]
        (is (var? v))
        (is (fn? (var-get v)))))))

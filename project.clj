(defproject jansi-clj "0.1.2-SNAPSHOT"
  :description "Clojure Wrapper around Jansi."
  :url "https://github.com/xsc/jansi-clj"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.fusesource.jansi/jansi "1.16"]]
  :license {:name "MIT"
            :url "https://choosealicense.com/licenses/mit"
            :comment "MIT License"
            :year 2021
            :key "mit"}
  :profiles {:doc {:plugins [[codox "0.10.3"]]
                   :codox {:exclude [jansi-clj.auto]
                           :src-dir-uri "https://github.com/xsc/jansi-clj/blob/master/"
                           :src-linenum-anchor-prefix "L"}}}
  :aliases {"doc" ["with-profile" "+doc" "doc"]}
  :pedantic? :abort)

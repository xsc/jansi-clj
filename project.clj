(defproject jansi-clj "1.0.2-SNAPSHOT"
  :description "Clojure Wrapper around Jansi."
  :url "https://github.com/xsc/jansi-clj"
  :license {:name "MIT"
            :url "https://choosealicense.com/licenses/mit"
            :comment "MIT License"
            :year 2021
            :key "mit"}
  :dependencies [[org.clojure/clojure "1.10.3" :scope "provided"]
                 [org.fusesource.jansi/jansi "2.4.0"]]
  :profiles {:doc {:plugins [[codox "0.10.3"]]
                   :codox {:exclude [jansi-clj.auto]
                           :src-dir-uri "https://github.com/xsc/jansi-clj/blob/master/"
                           :src-linenum-anchor-prefix "L"}}}
  :aliases {"doc" ["with-profile" "+doc" "doc"]}
  :pedantic? :abort)

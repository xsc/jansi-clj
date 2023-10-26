(defproject jansi-clj "1.0.2-SNAPSHOT"
  :description "Clojure Wrapper around Jansi."
  :url "https://github.com/xsc/jansi-clj"
  :license {:name "MIT"
            :url "https://choosealicense.com/licenses/mit"
            :comment "MIT License"
            :year 2021
            :key "mit"}
  :dependencies [[org.clojure/clojure "1.11.1" :scope "provided"]
                 [org.fusesource.jansi/jansi "2.4.1"]]
  :pedantic? :abort)

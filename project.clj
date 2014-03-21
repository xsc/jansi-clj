(defproject jansi-clj "0.1.1-SNAPSHOT"
  :description "Clojure Wrapper around Jansi."
  :url "https://github.com/xsc/jansi-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.fusesource.jansi/jansi "1.11"]]
  :profiles {:doc {:plugins [[codox "0.6.7"]]
                   :codox {:exclude [jansi-clj.auto]
                           :src-dir-uri "https://github.com/xsc/jansi-clj/blob/master/"
                           :src-linenum-anchor-prefix "L"}}}
  :aliases {"doc" ["with-profile" "+doc" "doc"]}
  :pedantic? :abort)

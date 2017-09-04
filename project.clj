(defproject jansi-clj "0.1.1"
  :description "Clojure Wrapper around Jansi."
  :url "https://github.com/xsc/jansi-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.fusesource.jansi/jansi "1.16"]]
  :profiles {:doc {:plugins [[codox "0.10.3"]]
                   :codox {:exclude [jansi-clj.auto]
                           :src-dir-uri "https://github.com/xsc/jansi-clj/blob/master/"
                           :src-linenum-anchor-prefix "L"}}}
  :aliases {"doc" ["with-profile" "+doc" "doc"]}
  :pedantic? :abort)

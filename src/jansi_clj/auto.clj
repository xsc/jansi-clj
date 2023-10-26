(ns jansi-clj.auto
  "Require this namespace to automatically set up your stdout/stderr streams
   for handling ANSI escape sequences."
  (:require [jansi-clj.core :refer [install!]]))

#_:clj-kondo/ignore
(defonce ^:private __init__
  (install!))

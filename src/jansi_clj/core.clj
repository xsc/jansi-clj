(ns jansi-clj.core
  (:require [clojure.string :as s])
  (:import [org.fusesource.jansi AnsiConsole Ansi Ansi$Color Ansi$Attribute]))

;; ## Helpers

(defn- collect-enum
  "Collect all Enum constants and create map associating a keyword with
   the respective constant."
  [^Class enum-class]
  (->> (for [v (.getEnumConstants enum-class)]
         (-> (str v)
             (s/lower-case)
             (keyword)
             (vector v)))
       (into {})))

(defn- add-symbol-meta
  "Add metadata to symbol in a macro-compliant way."
  [sym arglists docstring]
  (with-meta sym
             {:arglists (list 'quote arglists)
              :doc docstring}))

;; ## Reset

(defn reset
  "Reset colors."
  [^Ansi ansi]
  (.reset ansi))

;; ## Colors

(def ^:private __colors__
  "Map of JANSI Colors."
  (collect-enum Ansi$Color))

(defn colors
  "Get a seq of available colors."
  []
  (keys __colors__))

(defn- apply-color-fn
  "Create string using the given color and application function."
  [f color data]
  (if-let [c (__colors__ color)]
    (->> (map str data)
         (reduce
           (fn [^Ansi ansi ^String s]
             (-> ansi
                 (f c)
                 (.a s)))
           (Ansi/ansi))
         (reset)
         (str))
    (apply str data)))

(defn fg
  "Create string using the given foreground color."
  [color & data]
  (apply-color-fn
    #(.fg ^Ansi %1 %2)
    color data))

(defn fg-bright
  "Create string using the given foreground color."
  [color & data]
  (apply-color-fn
    #(.fgBright ^Ansi %1 %2)
    color data))

(defn bg
  "Create string using the given background color."
  [color & data]
  (apply-color-fn
    #(.bg ^Ansi %1 %2)
    color data))

(defn bg-bright
  "Create string using the given background color."
  [color & data]
  (apply-color-fn
    #(.bgBright ^Ansi %1 %2)
    color data))

(defn- create-jansi-form
  "Create function for JANSI access."
  [k suffix jansi-fn docstring-fmt]
  (let [f (add-symbol-meta
            (symbol (str (name k) suffix))
            '([& data])
            (format docstring-fmt (name k)))]
    `(def ~f
       (fn ~f
         [& data#]
         (apply ~jansi-fn ~k data#)))))

(defmacro ^:private create-colorize-functions!
  "Create colorization functions."
  []
  `(do
     ~@(for [color (colors)]
         (vector
           (create-jansi-form color "" `fg "Create string with foreground color '%s'.")
           (create-jansi-form color "-bright" `fg-bright "Create string with bright foreground color '%s'.")
           (create-jansi-form color "-bg" `bg "Create string with background color '%s'.")
           (create-jansi-form color "-bg-bright" `bg-bright "Create string with bright background color '%s'.")))
     nil))

(defn render
  "Render the given data based on JANSI-compatible color strings à la `@|color ...|@`."
  [& data]
  (let [^String s (apply str data)]
    (-> (Ansi/ansi)
        (.render s)
        (str))))

(defn renderf
  "Render the given data using `render` and the given format string and data."
  [fmt & data]
  (render (apply format fmt data)))

;; ## Attributes

(def ^:private __attributes__
  "Map of JANSI attributes."
  (->> (for [[attr v] (collect-enum Ansi$Attribute)
             :when (not= attr :reset)
             :let [^String s (name attr)
                   k (cond (.endsWith s "_off") (str "no-" (subs s 0 (- (count s) 4)))
                           (.endsWith s "_on") (subs s 0 (- (count s) 3))
                           :else (.replace s "_" "-"))]]
         [(keyword k) v])
       (into {})))

(defn attributes
  "Get a seq of available attribute keywords."
  []
  (keys __attributes__))

(defn a
  "Apply the given attribute to the given data."
  [attribute & data]
  (if-let [^Ansi$Attribute attr (__attributes__ attribute)]
    (->> (map str data)
         (reduce
           (fn [^Ansi ansi ^String s]
             (-> ansi
                 (.a attr)
                 (.a s)))
           (Ansi/ansi))
         (reset)
         (str))
    (apply str data)))

(defmacro ^:private create-attribute-functions!
  "Create attribute functions."
  []
  `(do
     ~@(for [attr (attributes)]
         (create-jansi-form attr "" `a "Create string with attribute '%s' set." ))
     nil))

;; ## Enable/Disable/Install/Uninstall

(defn enable!
  "Enable escape sequences."
  []
  (Ansi/setEnabled true))

(defn disable!
  "Disable escape sequences."
  []
  (Ansi/setEnabled false))

(defn install!
  "Install JANSI support into your application."
  []
  (AnsiConsole/systemInstall)
  ;; TODO: Memory Leak?
  (set! *out* (java.io.PrintWriter. System/out))
  true)

(defn uninstall!
  "Uninstall JANSI support from your application."
  []
  (AnsiConsole/systemUninstall)
  (set! *out* (java.io.PrintWriter. System/out))
  true)

;; ## Initialize

(defonce ^:private __init__
  (install!))

(create-colorize-functions!)
(create-attribute-functions!)

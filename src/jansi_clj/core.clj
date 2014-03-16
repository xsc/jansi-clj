(ns jansi-clj.core
  (:require [clojure.string :as s]
            [clojure.set :refer [rename-keys]])
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

(defn ansi
  "Get JANSI object."
  ^Ansi []
  (Ansi/ansi))

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
           (ansi))
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
    (-> (ansi)
        (.render s)
        (str))))

(defn renderf
  "Render the given data using `render` and the given format string and data."
  [fmt & data]
  (render (apply format fmt data)))

;; ## Attributes

(def ^:private __attributes__
  "Map of JANSI attributes."
  (-> (into {}
            (for [[attr v] (collect-enum Ansi$Attribute)
                  :when (not= attr :reset)
                  :let [^String s (name attr)
                        k (cond (.endsWith s "_off") (str "no-" (subs s 0 (- (count s) 4)))
                                (.endsWith s "_on") (subs s 0 (- (count s) 3))
                                :else (.replace s "_" "-"))]]
              [(keyword k) v]))
       (rename-keys
         {:intensity-bold :bold
          :no-intensity-bold :no-bold
          :intensity-faint :faint
          :no-intensity-faint :no-faint})))

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
           (ansi))
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

;; ## Screen

(defn erase-screen
  "Create string that can be used to erase the screen."
  []
  (-> (ansi)
      (.eraseScreen)
      (str)))

(defn erase-screen!
  "Print screen erase escape sequence."
  []
  (print (erase-screen)))

(defn erase-line
  "Create string that can be used to erase a line."
  []
  (-> (ansi)
      (.eraseLine)
      (str)))

(defn erase-line!
  "Print line erase escape sequence."
  []
  (print (erase-line)))

;; ## Cursor Movement

(defn cursor
  "Set cursor position."
  [^long x ^long y]
  (-> (ansi)
      (.cursor x y)
      (str)))

(defn cursor-down
  "Move cursor down."
  [^long y]
  (-> (ansi)
      (.cursorDown y)
      (str)))

(defn cursor-up
  "Move cursor up."
  [^long y]
  (-> (ansi)
      (.cursorUp y)
      (str)))

(defn cursor-left
  "Move cursor left."
  [^long x]
  (-> (ansi)
      (.cursorLeft x)
      (str)))

(defn cursor-right
  "Move cursor right."
  [^long x]
  (-> (ansi)
      (.cursorRight x)
      (str)))

(defn save-cursor
  "Save cursor position."
  []
  (-> (ansi)
      (.saveCursorPosition)
      (str)))

(defn restore-cursor
  "Restore cursor position."
  []
  (-> (ansi)
      (.restorCursorPosition)
      (str)))

;; ## Enable/Disable/Install/Uninstall

(defn enable!
  "Enable escape sequences."
  []
  (Ansi/setEnabled true))

(defn disable!
  "Disable escape sequences."
  []
  (Ansi/setEnabled false))

(defn- call-and-reset-out!
  [f]
  (let [old-stdout System/out]
    (f)
    (when (not= old-stdout System/out)
      (let [new-out (java.io.PrintWriter. System/out)]
        (or
          (try
            (do (set! *out* new-out) true)
            (catch Throwable _))
          (try
            (do (alter-var-root #'*out* (constantly new-out)) true)
            (catch Throwable _))
          (do (.close new-out) false))))))

(defn install!
  "Install JANSI support into your application."
  []
  (call-and-reset-out! #(AnsiConsole/systemInstall)))

(defn uninstall!
  "Uninstall JANSI support from your application."
  []
  (call-and-reset-out! #(AnsiConsole/systemUninstall)))

;; ## Create Functions

(create-colorize-functions!)
(create-attribute-functions!)

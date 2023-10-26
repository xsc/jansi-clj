(ns jansi-clj.core
  {:clj-kondo/config
   '{:lint-as {jansi-clj.core/def-screen-fns clojure.core/defn}}}
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
             (.a ^Ansi (f ansi c) s))
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
          :no-intensity_bold :no-bold
          :intensity-faint :faint})))

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

;; ## Definition Helper

(defmacro ^:private def-screen-fns
  "Define functions to manipulate screen/cursor. There'll be a function that
   just creates the string, and a `!`-suffixed one that prints the escape sequence."
  [sym docstring params method-call]
  (let [print-sym (symbol (str (name sym) "!"))]
    `(do
       (defn ~sym ~docstring
         ~params
         (-> (ansi)
             ~method-call
             (str)))
       (defn ~print-sym ~docstring
         ~params
         (-> (ansi)
             ~method-call
             (str)
             (print))))))

;; ## Screen

(def-screen-fns erase-screen
  "Erase the Screen."
  []
  (.eraseScreen))

(def-screen-fns erase-line
  "Erase a Line."
  []
  (.eraseLine))

;; ## Cursor Movement

(def-screen-fns cursor
  "Set cursor position."
  [^long x ^long y]
  (.cursor y x))    ; jansi takes row (y) first, column (x) second

(def-screen-fns cursor-down
  "Move cursor down."
  [^long y]
  (.cursorDown y))

(def-screen-fns cursor-up
  "Move cursor up."
  [^long y]
  (.cursorUp y))

(def-screen-fns cursor-left
  "Move cursor left."
  [^long x]
  (.cursorLeft x))

(def-screen-fns cursor-right
  "Move cursor right."
  [^long x]
  (.cursorRight x))

(def-screen-fns save-cursor
  "Save cursor position. Note: this issues both DEC and SCO escape sequences, for maximum compatibility across terminal emulators."
  []
  (.saveCursorPosition))

(def-screen-fns restore-cursor
  "Restore cursor position. Note: this issues both DEC and SCO escape sequences, for maximum compatibility across terminal emulators."
  []
  (.restorCursorPosition))

(def-screen-fns save-cursor-dec
  "Save cursor position. Note: DEC escape sequence only."
  []
  (.saveCursorPositionDEC))

(def-screen-fns restore-cursor-dec
  "Restore cursor position. Note: DEC escape sequence only."
  []
  (.restorCursorPositionDEC))

(def-screen-fns save-cursor-sco
  "Save cursor position. Note: SCO escape sequence only."
  []
  (.saveCursorPositionSCO))

(def-screen-fns restore-cursor-sco
  "Restore cursor position. Note: SCO escape sequence only."
  []
  (.restorCursorPositionSCO))

;; ## Enable/Disable/Install/Uninstall

(defn enable!
  "Enable escape sequences."
  []
  (Ansi/setEnabled true))

(defn disable!
  "Disable escape sequences."
  []
  (Ansi/setEnabled false))

(defn- reset-printer!
  "Reset printer if stream has changed."
  [k ^java.io.PrintStream old-stream ^java.io.PrintStream new-stream]
  (when (not= old-stream new-stream)
    (let [new-printer (java.io.PrintWriter. new-stream)]
      (or
        (try
          (if (= k :out)
            (set! *out* new-printer)
            (set! *err* new-printer))
          true
          (catch Throwable _))
        (try
          (alter-var-root
            (if (= k :out) #'*out* #'*err*)
            (constantly new-printer))
          true
          (catch Throwable _))))))

(defn- call-and-reset-out!
  "Call the given function, then reset stdout and stderr."
  [f]
  (let [old-stdout System/out
        old-stderr System/err]
    (f)
    (reset-printer! :out old-stdout System/out)
    (reset-printer! :err old-stderr System/err)))

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

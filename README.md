# jansi-clj

__jansi-clj__ is a Clojure wrapper around [jansi](https://github.com/fusesource/jansi), a
library that handles ANSI escape sequences (e.g. for terminal colors, cursor movement, ...)
on a per-OS basis, proclaiming that it _"even works on windows"_.

[![Build Status](https://travis-ci.org/xsc/jansi-clj.png?branch=master)](https://travis-ci.org/xsc/jansi-clj)
[![endorse](https://api.coderwall.com/xsc/endorsecount.png)](https://coderwall.com/xsc)

This library provides a dead-simple way of enabling ANSI support for all terminal emulators supported by
jansi, as well as functions to produce those ANSI sequences.

## Usage

__Leiningen__ ([via Clojars](https://clojars.org/jansi-clj))

```clojure
[jansi-clj "0.1.0-SNAPSHOT"] ;; unstable
```

## Auto-Enable ANSI Codes

If you just need ANSI support for different platforms but don't want to replace the code that actually generates
the escape sequences, just require the namespace `jansi-clj.auto`.

```clojure
(require 'jansi-clj.auto)
```

That's it. The standard output streams (as well as Clojure's `*out*` and `*err*` writers) will be wrapped
in a platform-/terminal-specific way to provide correct handling of ANSI codes. This means that you can
make any such console output portable without having to touch any existing code.

__Note:__ Wrapping the streams currently seems not to work in the REPL.

## Formatting Terminal Output

```clojure
(require '[jansi-clj.core :refer :all])
```

### Colors

```clojure
(colors)
;; => (:black :default :magenta :white :red :blue :green :yellow :cyan)
```

For each color, there exist four functions, e.g. `red` (create a string with red foreground), `red-bright`
(create a string with bright red foreground), as well as `red-bg` (red background) and `red-bg-bright`.

```clojure
(println "ERROR:" (red "This" " is " "a message."))
;; ERROR: This is a message.
;; => nil
```

As you can see, the functions behave like `str` regarding the concatenation of the parts.

### Attributes

```clojure
(attributes)
;; => (:underline-double :no-negative :no-underline :blink-fast :no-strikethrough
;;     :conceal :negative :no-italic :italic :faint :no-conceal :no-bold :no-blink
;;     :strikethrough :blink-slow :bold :underline)
```

For each of these keywords there exists a respective function that provides the desired formatting.

```clojure
(println (bold (red "ERROR")))
;; ERROR
;; => nil
```

### `render`

Jansi offers a special render syntax that can be used via `jansi-clj.core/render`:

```clojure
;; syntax: "@|code(,code)* text|@"
(render "@|green,bold Success!|@ (Code: 0)")
```

There is `renderf` that accepts a format string as its first parameter:

```clojure
(renderf "@|green,bold %s|@ (Code: %d)" "Sucess!" 0)
```

### Others

There is a variety of other functions that can be used. You can generate the respective documentation
using [codox](https://github.com/weavejester/codox):

```bash
$ lein doc
```

## Globally enable/disable ANSI Codes

The two functions `jansi-clj.core/enable!` and `jansi-clj.core/disable!` can be used to prevent the
different escape sequences from showing up in the generated strings in the first place.

## License

Copyright &copy; 2014 Yannick Scherer

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

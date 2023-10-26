# jansi-clj

__jansi-clj__ is a Clojure wrapper around [jansi](https://github.com/fusesource/jansi), a
library that handles ANSI escape sequences (e.g. for terminal colors, cursor movement, ...)
on a per-OS basis, proclaiming that it _"even works on windows"_.

[![Clojars Project](https://img.shields.io/clojars/v/jansi-clj.svg)](https://clojars.org/jansi-clj)
[![Documentation](https://cljdoc.org/badge/jansi-clj/jansi-clj)](https://cljdoc.org/d/jansi-clj/jansi-clj/CURRENT)
[![CI](https://github.com/xsc/jansi-clj/workflows/CI/badge.svg)](https://github.com/xsc/jansi-clj/actions?query=workflow%3ACI)

This library provides a dead-simple way of enabling ANSI support for all terminal emulators supported by
jansi, as well as functions to produce those ANSI sequences.

## Auto-Enable ANSI Codes

If you need ANSI support for different platforms but don't want to replace the code that actually generates
the escape sequences, just require the namespace `jansi-clj.auto`.

```clojure
(require 'jansi-clj.auto)
```

That's it. The standard output streams (as well as Clojure's `*out*` and `*err*` writers) will be wrapped
in a platform-/terminal-specific way to provide correct handling of ANSI codes. This means that you can
make any such console output portable without having to touch any existing code.

__Note:__ Wrapping the streams currently seems not to work correctly when done within a JVM spun up by Leiningen
since stdout/stderr are then redirected and Jansi cannot detect that there is a color-enabled terminal at their
ends. __This means that escape sequences will be filtered out when produced within `lein repl` or `lein run`.__
(`lein trampoline run` and executing an ubjerar should work, though.)

The choice of which colorization library to use is completely yours. But since you're already here...

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

```
MIT License

Copyright (c) 2021 Yannick Scherer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

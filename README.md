# lein-nashtest

#### Latest version
[![Clojars Project](https://img.shields.io/clojars/v/lein-nashtest.svg)](https://clojars.org/lein-nashtest)

Run CLJS tests in nashorn using cljs.test report hooks.

## Motivation
nashtest has two primary goals;

1. eliminate external dependencies (e.g. node, phantomjs, etc).
2. "clean" instrumentation of cljs.test/no third-party macros (e.g. deftest, defrunner, etc).

## Limitations

* ECMAScript support is limited to Nashorns capabilities.
  * DOM support is not available.
  * XMLHTTPRequest is not available.
  * Timers are not available.
  * Optimal target is ECMAScript 5.1.

Happy to accept PR's if people want to provide shims.

## Requirements

* Java 8.
* Leiningen 2.7.1+.
* lein-cljsbuild 1.1.7+.

## Basic Usage

```
$ lein nashtest

Testing jbx.events-test

Ran 1 tests containing 12 assertions.
0 failures, 0 errors.
```

## Installation

1) Add the plugin to your leiningen `project.clj` file.

```clj
  :plugins [[lein-nashtest "0.1.0-SNAPSHOT"]]
```

2) Configure your cljsbuilt test target in `project.clj`.

```clj
   :cljsbuild [{:id "test"
                :source-paths ["src/cljs" "src/cljc" "test/cljs"]
                :figwheel true
                :compiler {:language-in :ecmascript5-strict
                           :language-out :ecmascript5-strict
                           :optimizations :whitespace
                           :main jbx.gwp.runner
                           :asset-path "js/test"
                           :output-to  "target/cljsbuild/public/js/test.js"
                           :cache-analysis true}]
```
**Note**: Whitespace optimisation is required because document.write()

3) Configure nashtest as a root key in `project.clj`.

```clj
  :nashtest {:load-js "test.js"
             :runner "jbx.gwp.runner.run()"}
```

4) Create a cljs.test runner.

```clj
(ns jbx.runner
  (:require
    [cljs.test :as t :include-macros true]
    [jbx.events-test]))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (cljs.test/successful? m)
    (js/exit)
    (js/exit 1)))

(defn ^:export run
  []
  (enable-console-print!)
  (t/run-all-tests #"jbx.*-test"))
```

## Planned Work

* Introduce `:test-main` which would use Clojure style references (e.g. `jbx.runner/run`).
* File watcher.
* Naive document.write and <script> to allow an unoptimised CLJS build.
* Inject cljs.test/report defmethod from runner.
* JUnit XML output.

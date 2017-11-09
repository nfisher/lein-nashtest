# lein-nashtest [![Clojars Project](https://img.shields.io/clojars/v/lein-nashtest.svg)](https://clojars.org/lein-nashtest)

Run CLJS tests in nashorn using cljs.test report hooks.

## Motivation
nashtest has two primary goals;

1. eliminate external dependencies (e.g. node, phantomjs, etc).
2. "clean" instrumentation of cljs.test/no third-party macros (e.g. deftest, defrunner, etc).

## Limitations

* ECMAScript support is limited to Nashorns capabilities.
  * DOM support is not available (e.g. document.* is absent).
  * XMLHTTPRequest is not available.
  * Timers are not available.
  * Optimal target is ECMAScript 5.1.

Happy to accept PR's if people want to provide shims.

## Requirements

* Java 8.
* Leiningen 2.7.1+.
* whitespace compiled Javascript output file.

## Basic Usage

#### Single Execution

Single execution is intended for use in CI or as a precommit verification. If there are test failures the exit code will be 1, if all is green it will be 0.

```
$ lein nashtest

Testing jbx.events-test

Ran 1 tests containing 12 assertions.
0 failures, 0 errors.
```

#### Watch Loop Execution

Watch loop execution is intended for continuous feedback during development. A change to the :load-js file will result in automatic execution of the whole test suite.

```
$ lein nashtest watch
Watching for changes to test.js

Testing jbx.events-test

Ran 1 tests containing 12 assertions.
0 failures, 0 errors.
```

## Installation

1) Add the plugin to your leiningen `project.clj` file.

```clj
  :plugins [[lein-nashtest "0.1.3"]]
```

2) Configure your cljsbuild test target in `project.clj`.

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
**Note**: Whitespace optimisation is required because document.write().

3) Configure nashtest as a root key in `project.clj`.

```clj
  :nashtest {:load-js "test.js"
             :test-main "jbx.gwp.runner/run"}
```

4) Create a cljs.test runner.

```clj
(ns jbx.runner
  (:require
    [cljs.test :as t :include-macros true]
    [jbx.events-test]))

(defn ^:export run
  []
  (enable-console-print!)
  (t/run-all-tests #"jbx.*-test"))
```
**Note**: nashtest uses :test-main or :runner and will call your exported function as part of the test cycle. Do not include a call to the function at the bottom of your runner file.

## Planned Work

1. ~~Introduce `:test-main` which would use Clojure style references (e.g. `jbx.runner/run`).~~ [#1](https://github.com/nfisher/lein-nashtest/issues/1)
1. ~~File watcher.~~ [#2](https://github.com/nfisher/lein-nashtest/issues/2)
1. ~~Inject cljs.test/report defmethod from runner.~~ [#3](https://github.com/nfisher/lein-nashtest/issues/3)
1. Allow for multiple nashtest ids/targets.
1. JUnit XML output.
1. Naive document.write and <script> to allow an unoptimised CLJS build.

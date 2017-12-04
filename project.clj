(defproject lein-nashtest "0.1.3"
  :description "Run CLJS tests in nashorn using hooks for cljs.test."
  :url "https://github.com/nfisher/lein-nashtest"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojurescript "1.9.946" :scope "provided"]]

  :nashtest {:id "test"
             :load-js "target/cljsbuild/public/js/test.js"
             :test-main "jbx.runner/run"}

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [org.clojure/clojure]]
            [lein-figwheel "0.5.14" :exclusions [org.clojure/clojure]]]

  :cljsbuild {:builds
              [{:id "test"
                :source-paths ["cljs"]
                :figwheel false
                :compiler {:language-in :ecmascript5-strict
                           :language-out :ecmascript5-strict
                           :optimizations :whitespace
                           :main jbx.runner
                           :asset-path "js/test"
                           :output-to  "target/cljsbuild/public/js/test.js"
                           :cache-analysis true}}
               {:id "figtest"
                 :source-paths ["cljs"]
                 :figwheel true
                 :compiler {:language-in :ecmascript5-strict
                            :language-out :ecmascript5-strict
                            :optimizations :none
                            :main jbx.runner
                            :asset-path "js/test"
                            :output-dir "resources/public/js/test"
                            :output-to  "resources/public/js/test.js"
                            :cache-analysis true}}]}



  :profiles {:dev {:source-paths ["src" "test"]}}

  :eval-in-leiningen true)

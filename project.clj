(defproject lein-nashtest "0.1.0"
  :description "Run CLJS tests in nashorn using hooks for cljs.test."
  :url "https://github.com/nfisher/lein-nashtest"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :nashtest {:load-js "test.js"
             :runner "jbx.gwp.runner.run()"}
  :eval-in-leiningen true)

(ns leiningen.nashtest
  (:require
    [leiningen.core.main :as lein-main]
    [leiningen.nashtest.run :as run]
    [leiningen.nashtest.watch :as watch]))

(defn to-js [cljs]
  (-> cljs
      (clojure.string/replace #"/" ".")
      (clojure.string/replace #"-" "_")))

(defn ^:no-project-needed nashtest
  "Nashorn CLJS test runner."
  [project & keys]
  (let [{{:keys [load-js runner test-main]} :nashtest} project]
    (when (and (nil? runner) (nil? test-main))
      (lein-main/abort "nashtest: runner or test-main must be specified in your project.clj."))

    (when (and (nil? load-js))
      (lein-main/abort "nashtest: load-test must be specified in your project.clj."))

    (let [runner (if (nil? runner) (str (to-js test-main) "()") load-js)]
      (case (first keys)
        "once" (run/once load-js runner true)
        "watch" (watch/watch load-js runner)
        (run/once load-js runner true)))))

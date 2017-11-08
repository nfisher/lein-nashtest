(ns leiningen.nashtest
  (:require
    [leiningen.core.main :as lein-main])
  (:import
    javax.script.ScriptEngineManager
    javax.script.ScriptException))

(defonce polyfill "
\"use strict\";

var global = this;
var self = this;
var window = this;

function printer(prefix) {
  return function(msg) {
    print(prefix + msg);
  }
}

var console = {
  debug: printer('[DEBUG] '),
  warn: printer('[WARN] '),
  log: printer(''),
  error: printer('[ERROR] '),
  trace: printer('[TRACE] ')
};")

(defn to-js [cljs]
  (-> cljs
      (clojure.string/replace #"/" ".")
      (clojure.string/replace #"-" "_")))

(defn run-once [load-js runner]
  (try
    (let [engine (.getEngineByName (ScriptEngineManager.) "nashorn")]
      (.eval engine polyfill)
      (.eval engine (str "load('" load-js "');"))
      (.eval engine runner))
    (catch ScriptException se
      (.printStackTrace se))))

(defn ^:no-project-needed nashtest
  "Nashorn CLJS test runner."
  [project & keys]
  (let [{{:keys [load-js runner test-main]} :nashtest} project]
    (when (and (nil? runner) (nil? test-main))
      (lein-main/abort "nashtest: runner or test-main must be specified in your project.clj."))

    (when (and (nil? load-js))
      (lein-main/abort "nashtest: load-test must be specified in your project.clj."))

    (let [runner (if (nil? runner) (str (to-js test-main) "()") load-js)]
      (run-once load-js runner))))

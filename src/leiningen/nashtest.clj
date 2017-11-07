(ns leiningen.nashtest
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

(defn ^:no-project-needed nashtest
  "Nashorn CLJS test runner."
  [project & keys]
  (let [{{:keys [load-js runner]} :nashtest} project]
    (try
      (let [engine (.getEngineByName (ScriptEngineManager.) "nashorn")]
        (.eval engine polyfill)
        (.eval engine (str "load('" load-js "');"))
        (.eval engine runner))
      (catch ScriptException se
        (.printStackTrace se)))))

(ns leiningen.nashtest.run
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

(defn once
  "once loads and executes the runner specified by load-js."
  ([load-js runner exit?]
   (try
     (let [engine (.getEngineByName (ScriptEngineManager.) "nashorn")]
       (.eval engine polyfill)
       (when-not exit?
         (.eval engine "function exit(rc) { console.log('--');};"))
       (.eval engine (str "load('" load-js "');"))
       (.eval engine runner))
     (catch ScriptException se
       (.printStackTrace se)))))

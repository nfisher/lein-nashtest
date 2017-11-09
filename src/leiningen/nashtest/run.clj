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

(defonce cljstest-defmethod "
goog.provide('jbx.def');
cljs.core._add_method.call(null,cljs.test.report,new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(\"cljs.test\",\"default\",\"cljs.test/default\",-1581405322),new cljs.core.Keyword(null,\"end-run-tests\",\"end-run-tests\",267300563)], null),(function (m){
if(cljs.core.truth_(cljs.test.successful_QMARK_.call(null,m))){
return exit((0));
} else {
return exit((1));
}
}));
")

(defn once
  "once loads and executes the runner specified by load-js."
  ([load-js runner exit?]
   (try
     (let [engine (.getEngineByName (ScriptEngineManager.) "nashorn")]
       (.eval engine polyfill)
       (when-not exit?
         (.eval engine "function exit(rc) { console.log('--');};"))
       (.eval engine (str "load('" load-js "');"))
       (.eval engine cljstest-defmethod)
       (.eval engine runner))
     (catch ScriptException se
       (.printStackTrace se)))))

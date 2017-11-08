(ns leiningen.nashtest
  (:require
    [leiningen.core.main :as lein-main])
  (:import
    com.sun.nio.file.SensitivityWatchEventModifier
    java.nio.file.FileSystem
    java.nio.file.FileSystems
    java.nio.file.StandardWatchEventKinds
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

(defn run-once
  "run-once loads and executes the runner specified by load-js."
  [load-js runner]
  (try
    (let [engine (.getEngineByName (ScriptEngineManager.) "nashorn")]
      (.eval engine polyfill)
      (.eval engine (str "load('" load-js "');"))
      (.eval engine runner))
    (catch ScriptException se
      (.printStackTrace se))))

(def ^:private entry-create StandardWatchEventKinds/ENTRY_CREATE)
(def ^:private entry-modify StandardWatchEventKinds/ENTRY_MODIFY)
(def ^:private event-overflow StandardWatchEventKinds/OVERFLOW)
(def ^:private faster SensitivityWatchEventModifier/HIGH)

(defn- watch-file
  [load-js on-change]
  ; ref - https://docs.oracle.com/javase/tutorial/essential/io/notification.html
  (let [fs (cast FileSystem (FileSystems/getDefault))
        watch-service (.newWatchService fs)
        file-path (.toAbsolutePath (.getPath fs load-js (make-array String 0)))
        dir-path (.getParent file-path)
        path-key (.register dir-path watch-service (into-array (list entry-create entry-modify)) (into-array (list faster)))]
    (loop []
      (let [key (.take watch-service)
            events (.pollEvents key)]
        (doseq [event events
                :let [ctx (.context event)
                      kind (.kind event)
                      event-path (.resolve dir-path ctx)]]
          (when (every? true? [(not= event-overflow kind) (= event-path file-path)])
            (on-change))) ; need to implement the cljs.test report injection.

        (if (false? (.reset key))
          (lein-main/info "Directories disappeared and so am I!")
          (recur))))))

(defn watch
  "watch creates a file watcher on load-js and reloads the environment
   with run-once."
  [load-js runner]
  (lein-main/info (str "Watching for changes to " load-js))
  (watch-file load-js #(run-once load-js runner)))

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
        "once" (run-once load-js runner)
        "watch" (watch load-js runner)
        (run-once load-js runner)))))

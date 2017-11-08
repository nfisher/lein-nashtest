(ns leiningen.nashtest.watch
  (:require
    [leiningen.nashtest.run :as run]
    [leiningen.core.main :as lein-main])
  (:import
    com.sun.nio.file.SensitivityWatchEventModifier
    java.nio.file.FileSystem
    java.nio.file.FileSystems
    java.nio.file.StandardWatchEventKinds))

(def ^:private event-create StandardWatchEventKinds/ENTRY_CREATE)
(def ^:private event-modify StandardWatchEventKinds/ENTRY_MODIFY)
(def ^:private event-overflow StandardWatchEventKinds/OVERFLOW)
(def ^:private faster SensitivityWatchEventModifier/HIGH)

(defn- watch-file
  [load-js on-change]
  ; ref - https://docs.oracle.com/javase/tutorial/essential/io/notification.html
  (let [fs (cast FileSystem (FileSystems/getDefault))
        watch-service (.newWatchService fs)
        file-path (.toAbsolutePath (.getPath fs load-js (make-array String 0)))
        dir-path (.getParent file-path)
        path-key (.register dir-path watch-service (into-array (list event-create event-modify)) (into-array (list faster)))]
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
  (watch-file load-js #(run/once load-js runner false)))

(ns jbx.runner
  (:require
    [cljs.test :as t :include-macros true]
    [jbx.pants-test]))

(defn ^:export run
  []
  (enable-console-print!)
  (t/run-all-tests #"jbx.*-test"))

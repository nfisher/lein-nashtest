(ns leiningen.nashtest-test
  (:require
    [leiningen.nashtest :as nt]
    [clojure.test :as t :include-macros true]))

(t/deftest to-js
  (t/testing "correct conversion"
    (t/is (= "jbx.run" (nt/to-js "jbx/run")))
    (t/is (= "jbx.gwp.run" (nt/to-js "jbx.gwp/run")))
    (t/is (= "jbx.gwp.test_runner" (nt/to-js "jbx.gwp/test-runner")))
    (t/is (= "jbx_gwp.test_runner" (nt/to-js "jbx-gwp/test-runner")))))

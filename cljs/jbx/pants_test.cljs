(ns jbx.pants-test
  (:require
    [cljs.test :as t :include-macros true]))

(t/deftest test-is-wearing
  (t/testing "underwear when british"
    (t/is (= "pants" "pants") "yes pants is underwear")))

(ns rtl-test
  (:require [clojure.test :refer [deftest is testing]]
            [rtl]))
(deftest namespace-loads
  (testing "the restored CLJC namespace loads"
    (is (some? rtl))))

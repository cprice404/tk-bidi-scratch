(ns tk-bidi-scratch.tk-bidi-scratch-core-test
  (:require [clojure.test :refer :all]
            [tk-bidi-scratch.tk-bidi-scratch-core :refer :all]))

(deftest hello-test
  (testing "says hello to caller"
    (is (= "Hello, foo!" (hello "foo")))))

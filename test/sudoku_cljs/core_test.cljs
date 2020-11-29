(ns sudoku-cljs.core-test
  (:require
    [cljs.test     :refer-macros [deftest is run-tests]]
    [sudoku-cljs.core :as sudoku]))


(deftest test-peers
  (is (= 20 (count(sudoku/peers 19)))))



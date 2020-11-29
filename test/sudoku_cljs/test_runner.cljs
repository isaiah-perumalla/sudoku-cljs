(ns sudoku-cljs.test-runner
  (:require
   [sudoku-cljs.core-test ]
   [cljs.test :refer-macros [run-tests]]))

(run-tests 'sudoku-cljs.core-test)
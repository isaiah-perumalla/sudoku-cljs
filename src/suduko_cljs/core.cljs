(ns suduko-cljs.core
  (:require [reagent.dom :as rdom]))

(println "Hello world!")

(defn avg [x y]
  (/ (+ x y) 2.0))


(defn cell [pos val]
  (list 
   (when (= 0 (mod pos 9)) 
     [:br])
   [:div {:class "sudoku-board-cell"}
    [:input {:type "text" :pattern "\\d*" :id (str "input-" pos) :max-length 1 :defaultValue val}]]))

(defn suduko-board []
  [:div {:id "sudoku" :class "sudoku-board" :data-board-size 9}
   (map #(cell % (mod % 9)) (range 81)) ])

(defn ^:export render []
  (rdom/render [suduko-board] (js/document.getElementById "app")))

(render)


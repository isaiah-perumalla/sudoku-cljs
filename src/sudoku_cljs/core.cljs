
(ns ^:figwheel-hooks sudoku-cljs.core
  (:require [reagent.dom :as rdom]))

(def unit-list
  "9 units within a sudoku board, each unit has 9 positions"
  (let [units [[0 1 2] [3 4 5] [6 7 8]]]
    (vec
     (for [row units
           col units]
       (set
        (for [r row
              c col]
          (+ c (* 9 r))))))))


(defn pos-to-label
  [pos]
  (let [row (quot pos 9)
        col (rem pos 9)]
    (str (get "ABCDEFGHI" row) (inc col))))

(defn peers
  "given board state and position return vec of all valid values for that position"
  [pos]
  (let [row (quot pos 9)
        col (rem pos 9)]
    (reduce (fn [s position]
              (if (= pos position) s
                  (conj s position)))
            #{}
            (concat
             (range (* 9 row) (* 9 (inc row)))
             (range col 81 9)
             (first (filter #(some? (% pos)) unit-list))))))


(defn cell [pos state]
  (let [val (state pos)
        input-attrs {:type "text" :pattern "\\d*" :id (str "input-" pos) :max-length 1 :defaultValue val :readOnly (> val 0)}]
    (list
     (when (= 0 (mod pos 9))
       [:br {:key (str "b-" pos)}])
     [:div {:class (str "sudoku-board-cell" (when (= 0 val) " blank-cell")) :key pos :data-cell-value val}
      [:input input-attrs]])))



(defn suduko-board [state]
  [:div {:id "sudoku" :class "sudoku-board" :data-board-size 9}
   (map #(cell % state) (range 81))])

(def example-1  [0 0 3  0 2 0  6 0 0
                 9 0 0  3 0 5  0 0 1
                 0 0 1  8 0 6  4 0 0

                 0 0 8  1 0 2  9 0 0
                 7 0 0  0 0 0  0 0 8
                 0 0 6  7 0 8  2 0 0

                 0 0 2  6 0 9  5 0 0
                 8 0 0  2 0 3  0 0 9
                 0 0 5  0 1 0  3 0 0])

(defn ^:export render []
  (rdom/render [suduko-board example-1] (js/document.getElementById "app")))

(render)



(defn mount []
  (rdom/render [suduko-board example-1] (js/document.getElementById "app")))


(defn ^:after-load re-render []
  (mount))


(defonce start-up (do (mount) true))

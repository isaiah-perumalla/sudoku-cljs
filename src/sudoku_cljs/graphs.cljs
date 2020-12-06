(ns ^:figwheel-hooks sudoku-cljs.graphs
    (:require [goog.labs.format.csv :as csv]
              [goog.fs :as fs]
              [goog.events :as events]
              [reagent.dom :as rdom]
              [reagent.core :as r]
              [cljs.core.async :as async
               :refer [<! >! go go-loop chan close! sliding-buffer  alts!]]))


(defn by-id
  "Short-hand for document.getElementById(id)"
  [id]
  (.getElementById js/document id))

(defn events->chan
  "Given a target DOM element and event type return a channel of
  observed events. Can supply the channel to receive events as third
  optional argument."
  ([el event-type] (events->chan el event-type (chan)))
  ([el event-type c]
   (events/listen el event-type
                  (fn [e] (async/put! c e)))
   c))


(def app-state (r/atom {}))

(def first-file
  (map (fn [e]
         (let [target (.-currentTarget e)
               file (-> target .-files (aget 0))]
           (set! (.-value target) "")
           file))))

(def parse-graph
  (map (fn [e]
         (let [file (.-result (.-target e))
               ]
           (.log js/console file)
           file))))


(def files-chan (chan 1 first-file))
(def file-reads (chan 1 parse-graph))
(def decoder (js/TextDecoder.))
(def encoder (js/TextEncoder.))

(defn bytes->str 
  ([uint8-array] 
  (.decode decoder uint8-array))
  ([uint8-array start end]
  (.decode decoder (.slice uint8-array start end))))

(defn str->bytes 
  [s]
  (.encode encoder s))


(defn line-break? 
  [b]
  (= 10 b))


  
(defn bytes->lines
  [bytes prefix]
  (let [size (.-length bytes)
        last-ch (aget bytes (dec size))
        lines (.split (bytes->str bytes 0 size) "\n")]
    (aset lines 0 (str prefix (aget lines 0)))
    (if (= 10 last-ch)
      [(vec lines) ""]
      (let [next (.pop lines)]
        [(vec lines) next]))))


(defn parse-lines 
  "transducer which takes chunks of bytes and returns lines"
  []
  (fn [xf]
    (let [prefix (volatile! "")]
      (fn
        ([] xf)
        ([acc]
         (let [pre @prefix]
           (vreset! prefix "") 
           (if (= pre "")
             acc
             (xf acc [pre]))))
        ([acc input]
         (let [[lines rem] (bytes->lines input @prefix)]
           (vreset! prefix rem)
           (xf acc lines)))))))


(defn lines-from-chunk
  [rem bytes]
   (loop [start 0
          lines []
          prefix rem]
     (let [linebreak (.indexOf bytes 10, start)
           size (.-length bytes) ]
       (cond
         (= -1 linebreak) [lines (bytes->str bytes start size)]
         (= start size) [lines ""]
          :else 
             (recur (inc linebreak) 
                    (conj lines (str prefix (bytes->str bytes start linebreak)))
                    "")))))
  
  
(defn lines-from
  [file chunk-size]
  (let [size (.-size file)
        reader (js/FileReader.)
        chunks (chan)
        out (chan)]
    (go (loop [offset 0
               line ""]
         (println "start loop")
          (if (< offset size)
            (do
              (set! (.-onload reader) #(>! chunks (lines-from-chunk line (js/Uint8Array. (.-result (.-target %))))))
              (.readAsArrayBuffer reader (.slice file offset (+ offset chunk-size)))
              (let [[lines rem] (<! chunks)]
                (>! out lines)
                (recur (+ offset chunk-size) rem)))
            (async/close! out))))
    out))
    
    

(defn put-upload [e]
  (async/put! files-chan e))

(defn upload-btn [file-name]
  [:span.upload-label
   [:label
    [:input.hidden-xs-up
     {:type "file" :accept ".txt" :on-change put-upload}]
    [:i.fa.fa-upload.fa-lg]
    (or file-name "click here to upload and render csv...")]
   (when file-name
     [:i.fa.fa-times {:on-click #(reset! app-state {})}])])

(go-loop []
  (let [reader (js/FileReader.)
        file (<! files-chan)]
    (swap! app-state assoc :file-name (.-name file))
    (set! (.-onload reader) #(async/put! file-reads %))
    (.readAsText reader file)
    (recur)))

(go-loop []
  (swap! app-state assoc :data (<! file-reads))
  (recur))

(defn ^:export render-graph-upload [ele-id]
  (rdom/render [upload-btn "click here to upload and render txt"] (js/document.getElementById ele-id)))
(ns ^:integration rhizome.viz-test
  (:require
   [clojure.test :refer :all]
   [rhizome.dot :as dot]
   [rhizome.img :as img]
   [rhizome.viz :as viz]))

(def g
  {:a [:b :c]
   :b [:c]
   :c [:a]})

(def t-0
  [[1 [2 3]] [4 [5]]])

(def t-1
  '([1 2] ([3 4] ([5 6 7]))))

(def t-3
  {1 [2 3 4 5 7 14 24 25]
   2 []
   3 [6 8 9 10 11 13 16 18 19 20 22]
   4 [23]
   5 []
   6 [15]
   7 [17]
   8 []
   9 []
   10 [12]
   11 []
   12 [21]
   13 []
   14 []
   15 [19]
   16 []
   17 []
   18 []
   19 []
   20 []
   21 []
   22 []
   23 []
   24 []
   25 []})

(def pause 2000)

(deftest view-graph
  (viz/view-graph (keys g) g
    :options {:dpi 500}
    :node->descriptor (fn [n] {:label n})
    :edge->descriptor (fn [src dst] {:label dst}))
  (Thread/sleep pause)

  (viz/view-graph (keys g) g
    :node->descriptor (fn [n] {:label n})
    :node->cluster identity
    :cluster->parent {:a :b})
  (Thread/sleep pause))

(deftest view-tree
  (viz/view-tree sequential? seq t-0
    :node->descriptor (fn [n] {:label (when (number? n) (str n))}))
  (Thread/sleep pause)

  (viz/view-tree sequential? seq t-0
    :node->descriptor (fn [n] {:label (when (number? n) (str n))})
    :node->cluster (fn [n] (when (number? n) (rem n 2)))
    :cluster->descriptor (fn [n] {:label (if (even? n) "even" "odd")}))
  (Thread/sleep pause)

  (viz/view-tree list? seq t-1
    :node->descriptor (fn [n] {:label (when (vector? n) n)}))
  (Thread/sleep pause)

  (viz/view-tree list? seq t-1
    :node->descriptor (fn [n] {:label (when (vector? n) n)})
    :vertical? false)
  (Thread/sleep pause))

(deftest view-dot
  (viz/view-dot (keys t-3) t-3
    :directed? false
    :node->descriptor (fn [n] {:label n}))
  (Thread/sleep pause))

(deftest view-twopi
  (viz/view-twopi (keys t-3) t-3
    :directed? false
    :node->descriptor (fn [n] {:label n}))
  (Thread/sleep pause))

(deftest view-neato
  (viz/view-neato (keys t-3) t-3
    :directed? false
    :node->descriptor (fn [n] {:label n}))
  (Thread/sleep pause))

(deftest view-fdp
  (viz/view-fdp (keys t-3) t-3
    :directed? false
    :node->descriptor (fn [n] {:label n}))
  (Thread/sleep pause))

(deftest view-circo
  (viz/view-circo (keys t-3) t-3
    :directed? false
    :node->descriptor (fn [n] {:label n}))
  (Thread/sleep pause))

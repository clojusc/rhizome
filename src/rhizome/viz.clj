(ns rhizome.viz
  (:require
   [clojure.string :as str]
   [clojure.java.shell :as sh]
   [clojure.java.io :as io]
   [rhizome.dot :as dot]
   [rhizome.img :as img])
  (:import
   (java.awt Toolkit Dimension)
   (java.awt.event KeyEvent WindowAdapter)
   (javax.swing AbstractAction JComponent JFrame JLabel JScrollPane ImageIcon KeyStroke)
   (javax.script ScriptEngineManager)))

;;; Backwards compatibility

(def dot->image img/dot->image)
(def dot->svg img/dot->svg)
(def save-image img/save-image)
(def graph->image img/graph->image)
(def graph->svg img/graph->svg)
(def save-graph img/save-graph)
(def tree->image img/tree->image)
(def tree->svg img/tree->svg)
(def save-tree img/save-tree)

(defn headless? []
  (= "true" (System/getProperty "java.awt.headless")))

(when-not (headless?)

  (def ^:private shortcut-mask
    (.. Toolkit getDefaultToolkit getMenuShortcutKeyMask))

  (def ^:private close-key
    (KeyStroke/getKeyStroke KeyEvent/VK_W (int shortcut-mask))))

(defn create-frame
  "Creates a frame for viewing graphviz images.  Only useful if you don't want
  to use the default frame."
  [{:keys [name close-promise dispose-on-close?]}]
  (delay
    (let [frame (JFrame. ^String name)
          image-icon (ImageIcon.)
          pane (-> image-icon JLabel. JScrollPane.)]
      (doto pane
        (.. (getInputMap JComponent/WHEN_IN_FOCUSED_WINDOW)
          (put close-key "closeWindow"))
        (.. getActionMap
          (put "closeWindow"
            (proxy [AbstractAction] []
              (actionPerformed [e]
                (.setVisible frame false))))))
      (doto frame
        (.addWindowListener
          (proxy [WindowAdapter] []
            (windowClosing [e]
              (.setVisible frame false)
              (when dispose-on-close?
                (.dispose frame))
              (when close-promise
                (deliver close-promise true)))))
        (.setContentPane pane)
        (.setSize 1024 768)
        (.setDefaultCloseOperation javax.swing.WindowConstants/HIDE_ON_CLOSE))


      [frame image-icon pane])))

(def default-frame (create-frame {:name "rhizome"}))

(defn- send-to-front
  "Makes absolutely, completely sure that the frame is moved to the front."
  [^JFrame frame]
  (doto frame
    (.setExtendedState JFrame/NORMAL)
    (.setAlwaysOnTop true)
    .repaint
    .toFront
    .requestFocus
    (.setAlwaysOnTop false))

  ;; may I one day be forgiven
  (when-let [applescript (.getEngineByName (ScriptEngineManager.) "AppleScript")]
    (try
      (.eval applescript "tell me to activate")
      (catch Throwable e
        ))))

(defn view-image
  "Takes an `image`, and displays it in a window.  If `frame` is not specified,
  then the default frame will be used."
  ([image]
     (view-image default-frame image))
  ([frame image]
     (let [[^JFrame frame ^ImageIcon image-icon ^JLabel pane] @frame]
       (.setImage image-icon image)
       (.setVisible frame true)
       (java.awt.EventQueue/invokeLater
         #(send-to-front frame)))))

(def view-graph
  "Takes a graph descriptor in the style of `graph->dot`, and displays a
  rendered image."
  (comp view-image img/dot->image dot/graph->dot))

(def view-tree
  "Takes a tree descriptor in the style of `tree->dot`, and displays a rendered
  image."
  (comp view-image img/dot->image dot/tree->dot))

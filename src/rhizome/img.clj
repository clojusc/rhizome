(ns rhizome.img
  (:require
   [clojure.string :as str]
   [clojure.java.shell :as sh]
   [clojure.java.io :as io]
   [rhizome.dot :as dot])
  (:import
   (java.awt.image RenderedImage)
   (javax.imageio ImageIO)))

(defn- format-error [s err]
  (apply str
    err "\n"
    (interleave
      (map
        (fn [idx s]
          (format "%3d: %s" idx s))
        (range)
        (str/split-lines s))
      (repeat "\n"))))

(defn dot->image
  "Takes a string containing a GraphViz dot file, and renders it to an image.
  This requires that GraphViz is installed on the local machine."
  [s]
  (let [{:keys [out err]} (try
                            (sh/sh "dot" "-Tpng" :in s :out-enc :bytes)
                            (catch java.io.IOException e
                              (try
                                (sh/sh "dot" "-v")
                                (throw e) ;; dot is working fine, something else is broken
                                (catch java.io.IOException e
                                  (throw (RuntimeException.
                                          "Couldn't find `dot` executable: have you installed graphviz?"
                                          e))))))]
    (or
      (ImageIO/read (io/input-stream out))
      (throw (IllegalArgumentException. ^String (format-error s err))))))

(defn dot->svg
  "Takes a string containing a GraphViz dot file, and returns a string
  containing SVG. This requires that GraphViz is installed on the local
  machine."
  [s]
  (let [{:keys [out err]} (sh/sh "dot" "-Tsvg" :in s)]
    (or
      out
      (throw (IllegalArgumentException. ^String (format-error s err))))))

(defn save-image
  "Saves the given image buffer to the given filename. The default
  file type for the image is png, but an optional type may be supplied
  as a third argument."
  ([image filename]
     (save-image image "png" filename))
  ([^RenderedImage image ^String filetype filename]
     (ImageIO/write image filetype (io/file filename))))

(def graph->image
  "Takes a graph descriptor in the style of `graph->dot`, and returns a
  rendered image."
  (comp dot->image dot/graph->dot))

(def graph->svg
  "Takes a graph descriptor in the style of `graph->dot`, and returns SVG."
  (comp dot->svg
        (fn [nodes adjacent & {:as opts}]
          (let [final-opts (update-in opts [:options :dpi] #(if % % 72))]
            (apply dot/graph->dot nodes adjacent (apply concat final-opts))))))

(defn save-graph
  "Takes a graph descriptor in the style of `graph->dot`, and saves the image
  to disk."
  [nodes adjacent & {:keys [filename] :as options}]
  (-> (apply dot/graph->dot nodes adjacent (apply concat options))
    dot->image
    (save-image filename)))

(def tree->image
  "Takes a tree descriptor in the style of `tree->dot`, and returns a rendered
  image."
  (comp dot->image dot/tree->dot))

(def tree->svg
  "Takes a tree descriptor in the style of `tree->dot`, and returns SVG."
  (comp dot->svg dot/tree->dot))

(defn save-tree
  "Takes a graph descriptor in the style of `graph->dot`, and saves the image
  to disk."
  [branch? children root & {:keys [filename] :as options}]
  (-> (apply dot/tree->dot branch? children root (apply concat options))
    dot->image
    (save-image filename)))

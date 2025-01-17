![](/docs/rhizome.jpg)

Rhizome is a library for visualizing graph and tree structures.

## Usage

To include in your project, add this to your `project.clj`:

```clj
[clojusc/rhizome "0.3.0-SNAPSHOT"]
```


Use of this project requires that [Graphviz](http://www.graphviz.org) is installed, which can be checked by running `dot -V` at the command line.  If it's not installed, you can do the following:

| platform | directions |
|----------|------------|
| Linux | install `graphviz` using your package manager |
| OS X | use `brew install graphiz` if you use Homebrew, or `sudo port install graphviz` if you use MacPorts |
| Windows | [download the installer](https://graphviz.gitlab.io/_pages/Download/Download_windows.html) |

There are three namespaces, `rhizome.dot`, `rhizome.img`, and `rhizome.viz`.  The former will take a graph and return a string representation of a Graphviz dot file, the second takes graphs and renders them, the last one displays graphs.  In practice, you will likely be using one of the latter two.

The core function is `rhizome.viz/view-graph`.  It takes two parameters: `nodes`, which is a list of nodes in the graph, and `adjacent`, which is a function that takes a node and returns adjacent nodes.  Nodes can be of any type you like, as long as the objects in `nodes` and the objects returned by `adjacent` are equivalent.

These can be followed by zero or more of the following keyword arguments:


| name | description |
|------|-------------|
| `:directed?` | whether the graph should be rendered as a directed graph, defaults to true |
| `:vertical?` | whether the graph should be rendered top-to-bottom, defaults to true |
| `:node->descriptor` | takes a node, and returns a map of attributes onto values describing how the node should be rendered |
| `:edge->descriptor` | takes the source and destination node, and returns a map of attributes onto values describing how the edge should be rendered |
| `:options` | a map of attributes onto values describing how the graph should be rendered |
| `:node->cluster` | takes a node and returns which cluster, if any, the node belongs to |
| `:cluster->parent` | takes a cluster and returns which cluster, if any, it is contained within |
| `:cluster->descriptor` | takes a cluster and returns a map of attributes onto values describing how the cluster should be rendered |

The rendering attributes described by `:node->descriptor`, `:edge->descriptor`, `:cluster->descriptor`, and `:options` are described in detail [here](https://graphviz.gitlab.io/_pages/doc/info/attrs.html).  String and keyword values are interchangeable.

The most commonly-used attributes are `label`, which describes the text overlaid on a node, edge, or cluster, and `shape`, the options for which are described [here](https://graphviz.gitlab.io/_pages/doc/info/shapes.html).  For the `:options`, it's sometimes useful to adjust the `dpi`, which controls the size of the image.

An example:

```clj
> (require '[rhizome.viz :refer :all)
> (require '[rhizome.img :refer :all)
nil
> (def g
    {:a [:b :c]
     :b [:c]
     :c [:a]})
#'g
> (view-graph (keys g) g
    :node->descriptor (fn [n] {:label n}))
```

![](/docs/example_graph.png)

Clusters are a way of grouping certain nodes together.  They can be any object you like, including values also used by a node.  Using `:cluster->parent`, they can be nested:

```clj
> (view-graph (keys g) g
    :cluster->descriptor (fn [n] {:label n})
    :node->cluster identity
    :cluster->parent {:b :c, :a :c})
```

![](/docs/example_cluster_graph.png)

While trees are a special case of graphs, using `view-graph` to visualize trees can be a little indirect.  To make this simpler, there's a `view-tree` function, which is modeled after Clojure's `tree-seq` operator.  This function takes three parameters, `branch?`, `children`, and `root`, followed by zero or more of the keyword arguments taken by `view-graph`.  This can make it easy to visualize hierarchical structures:

```clj
> (def t [[1 [2 3]] [4 [5]]])
#'t
> (view-tree sequential? seq t
    :node->descriptor (fn [n] {:label (when (number? n) n)}))
```

![](/docs/example_tree.png)

If the value for `label` is not a string, typically it will be displayed as a string representation of the value.  However, if the value is sequential, then the node will be displayed as a `Record` type:

```clj
> (def t '([1 2] ([3 4] ([5 6 7]))))
#'t
> (view-tree list? seq t
    :node->descriptor (fn [n] {:label (when (vector? n) n)}))
```

![](/docs/tree_record_example.png)

`rhizome.viz/graph->svg` can be used to render the graph as SVG.

There are also `view-*` functions for each of Graphviz's layout engines. For
example:

```clj
> (def t
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
> (viz/view-twopi (keys t) t
    :directed? false
    :node->descriptor (fn [n] {:label n}))
```
![](/docs/view_twopi_example.png)

```clj
> (viz/view-fdp (keys t) t
    :directed? false
    :node->descriptor (fn [n] {:label n}))
```
![](/docs/view_fdp_example.png)

## License

Copyright © 2013 Zachary Tellman

Copyright © 2019 Clojure-Aided Enrichment Center

Distributed under the [MIT License](http://opensource.org/licenses/MIT)

(defproject clojusc/rhizome "0.3.0-SNAPSHOT"
  :description "A simple way to visualize graphs"
  :license {
    :name "MIT License"
    :url "http://opensource.org/licenses/MIT"}
  :dependencies []
  :global-vars {*warn-on-reflection* true}
  :plugins [[codox "0.6.4"]]
  :codox {
    :writer codox-md.writer/write-docs}
  :profiles {
    :dev {
      :dependencies [
        [org.clojure/clojure "1.10.1"]
        [codox-md "0.2.0" :exclusions [org.clojure/clojure]]]}
    :ubercompile {
      :aot :all}}
  :aliases {
    "ubercompile" ["with-profile" "+ubercompile" "compile"]
    "build" ["do"
      ["clean"]
      ["ubercompile"]
      ["clean"]
      ["uberjar"]]})

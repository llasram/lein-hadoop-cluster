(ns lein-hadoop-cluster.plugin
  (require [clojure.string :as str]
           [clojure.java.io :as io]
           [clojure.java.shell :refer [sh]]
           [leiningen.core.project :as lcp]
           [leiningen.core.main :as main]))

(def hadoop-dependencies
  "Hadoop-provided dependencies which can cause problems if doubled up on the
classpath."
  '[org.apache.hadoop/hadoop-common
    org.apache.hadoop/hadoop-client
    org.apache.hadoop/hadoop-core
    org.apache.hadoop/hadoop-hdfs
    org.slf4j/slf4j-api
    org.slf4j/slf4j-log4j12
    log4j/log4j])

(defmacro ignore-errors
  "Returns the result of evaluating body, or nil if it throws an exception."
  [& body] `(try ~@body (catch java.lang.Exception _# nil)))

(def hadoop-classpaths
  "System Hadoop installation classpath components."
  (let [{:keys [out exit]} (ignore-errors (sh "hadoop" "classpath"))]
    (if (and out (zero? exit))
      (remove str/blank? (map #(.trim ^String %) (.split ^String out ":")))
      (main/debug "error running `hadoop classpath`"))))

(def hbase-classpaths
  "System HBase installation classpath components."
  (let [{:keys [out exit]} (ignore-errors (sh "hbase" "classpath"))]
    (if (and out (zero? exit))
      (remove str/blank? (map #(.trim ^String %) (.split ^String out ":")))
      (main/debug "error running `hbase classpath`"))))

(def hadoop-checknative
  "Native library path determined via `hadoop checknative`."
  (let [{:keys [out exit]} (ignore-errors (sh "hadoop" "checknative"))]
    (if (and out (zero? exit))
      (->> out (re-find #"(?m) (/.*libhadoop.*)$") second io/file .getParent)
      (main/debug "error running `hadoop checknative`"))))

(def native-path
  "Most common (Linux) path for native dependencies."
  (or hadoop-checknative
      (let [hadoop-home (or (System/getenv "HADOOP_HOME") "/usr/lib/hadoop")]
        (str (io/file hadoop-home "lib/native")))))

(def hadoop-system-profile
  "Profile map for the `hadoop-system` profile."
  (cond-> {:exclusions hadoop-dependencies
           :plugins '[[lein-extend-cp "0.1.0"]]
           :lein-extend-cp {:paths (concat hadoop-classpaths hbase-classpaths)}}
          (.exists (io/file native-path))
          , (assoc :jvm-opts [(str "-Djava.library.path=" native-path)])))

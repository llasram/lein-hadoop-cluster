(ns lein-hadoop-cluster.plugin
  (require [clojure.java.io :as io]
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

(def hadoop-classpaths
  "System Hadoop installation classpath components."
  (try
    (-> (sh "hadoop" "classpath")
        :out (.split ":") (->> (map #(.trim %)))
        vec)
    (catch Exception _
      (main/info "lein-hadoop-cluster: Error running `hadoop classpath`.")
      [])))

(def hbase-classpaths
  "System HBase installation classpath components."
  (try
    (-> (sh "hbase" "classpath")
        :out (.split ":") (->> (map #(.trim %)))
        vec)
    (catch Exception _
      (main/info "lein-hadoop-cluster: Error running `hbase classpath`.")
      [])))

(def hadoop-checknative
  "Native library path determined via `hadoop checknative`."
  (try
    (->> (sh "hadoop" "checknative") :out
         (re-find #"(?m) (/.*libhadoop.*)$") second
         io/file .getParent)
    (catch Exception _
      nil)))

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

(defn inject-profile
  [project]
  (-> project
      (update-in [:profiles :hadoop-system]
                 (fnil #'lcp/meta-merge {}) hadoop-system-profile)
      (cond-> (not (get-in project [:profiles :hadoop-cluster]))
              , (assoc-in [:profiles :hadoop-cluster]
                          [:base :system :user :dev
                           :hadoop-system :hadoop-user])
              (not (get-in project [:profiles :hadoop-user]))
              , (assoc-in [:profiles :hadoop-user] {}))
      (assoc-in [:aliases "hadoop-repl"]
                ["with-profile" "hadoop-cluster" "repl"])
      (assoc ::hadoop-cluster true)))

(defn middleware
  [project]
  (if (::hadoop-cluster project)
    project
    (let [pwop (:without-profiles (meta project) project)]
      (with-meta (inject-profile project)
        (assoc (meta project)
          :without-profiles (inject-profile pwop))))))

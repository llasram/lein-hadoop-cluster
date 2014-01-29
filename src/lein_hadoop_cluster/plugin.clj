(ns lein-hadoop-cluster.plugin
  (require [clojure.java.io :as io]
           [clojure.java.shell :refer [sh]]
           [leiningen.core.project :as lcp]))

(def hadoop-dependencies
  '[org.apache.hadoop/hadoop-common
    org.apache.hadoop/hadoop-client
    org.apache.hadoop/hadoop-core
    org.apache.hadoop/hadoop-hdfs
    org.slf4j/slf4j-api
    org.slf4j/slf4j-log4j12
    log4j/log4j])

(def hadoop-classpaths
  (-> (sh "hadoop" "classpath")
      :out (.split ":") (->> (map #(.trim %)))
      vec))

(def native-path
  "Most common (Linux) path for native dependencies."
  "/usr/lib/hadoop/lib/native")

(def hadoop-cluster-profile
  (cond-> {:exclusions hadoop-dependencies
           :plugins '[[lein-extend-cp "0.1.0"]]
           :lein-extend-cp {:paths hadoop-classpaths}}
          (.exists (io/file native-path))
          , (assoc :jvm-opts [(str "-Djava.library.path=" native-path)])))

(defn inject-profile
  [project]
  (-> project
      (update-in [:profiles :hadoop-system]
                 (fnil #'lcp/meta-merge {}) hadoop-cluster-profile)
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

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
  [project pname profile]
  (-> (assoc-in project [:profiles pname] profile)
      (assoc ::hadoop-cluster true)))

(defn middleware
  [project]
  (if (::hadoop-cluster project)
    project
    (let [pwop (:without-profiles (meta project) project)
          profiles (:profiles pwop {})
          pname (:hadoop-cluster-profile pwop :hadoop-cluster)
          profile (->> (get profiles pname {})
                       (#'lcp/meta-merge hadoop-cluster-profile))]
      (with-meta (inject-profile project pname profile)
        (assoc (meta project)
          :without-profiles (inject-profile pwop pname profile))))))

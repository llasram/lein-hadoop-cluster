{:hadoop-system #=(eval (do
                          (require 'lein-hadoop-cluster.plugin)
                          lein-hadoop-cluster.plugin/hadoop-system-profile)),
 :hadoop-cluster [:base :system :user :dev
                  :plugin.lein-hadoop-cluster/hadoop-system],
 }

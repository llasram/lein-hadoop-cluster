{:default [:base :system :user :dev :plugin.lein-hadoop-cluster/system],
 :system #=(eval (do
                   (require 'lein-hadoop-cluster.plugin)
                   lein-hadoop-cluster.plugin/hadoop-system-profile)),
 }

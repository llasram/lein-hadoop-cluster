# lein-hadoop-cluster

A Leiningen plugin for running tasks against a live Hadoop cluster.  Adds a
profile named `hadoop-cluster` (or a user-provided name) which includes the
correct class path etc for the currently-configured Hadoop cluster.

## Usage

Put `[lein-hadoop-cluster "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
lein-hadoop-cluster 0.1.0-SNAPSHOT`.

Then you can run tasks under the `hadoop-cluster` profile, or otherwise use the
profile to access the live cluster task/JVM configuration.  For example:

    $ lein with-profile -provided,+hadoop-cluster repl

## License

Copyright © 2014 Marshall Bockrath-Vandegrift

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

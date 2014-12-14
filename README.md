# lein-hadoop-cluster

A Leiningen plugin for running tasks against a live Hadoop cluster.  Adds a
profile named `hadoop-cluster` which includes the correct class path etc for the
currently-configured Hadoop cluster.

## Usage

Put `[lein-hadoop-cluster "0.1.4"]` into the `:plugins` vector of your `:user`
profile, or if you are on Leiningen 1.x do `lein plugin install
lein-hadoop-cluster 0.1.4`.

Then you can run tasks under the `hadoop-cluster` profile, or otherwise use the
profile to access the live cluster task/JVM configuration.  For example:

    $ lein with-profile hadoop-cluster repl

The `repl` command is the primary intended use of the plugin, and so the above
is actually provided as the `hadoop-repl` alias:

    $ lein hadoop-repl

Internally, the plugin defines and makes use of three Leiningen profiles:

- `hadoop-cluster` – Intended as a replacement for `default`.  By default
  includes the profiles `system`, `base`, `dev`, `user`, `hadoop-system`, and
  `hadoop-user`.  May be defined by the user in `project.clj` instead.
- `hadoop-user` – User hook for easily adding e.g. additional cluster-Hadoop
  version-specific dependencies.  By default empty.
- `hadoop-system` – Additive profile containing the actual configuration
  necessary to access the system Hadoop installation.

Additionally, the plugin expects that project files will specify all their
Hadoop-provided dependencies via the `provided` profile.  Placing them in
e.g. `dev` instead will result in them appearing on the `hadoop-cluster` profile
classpath, and before the system Hadoop.

## License

Copyright © 2014 Marshall Bockrath-Vandegrift

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

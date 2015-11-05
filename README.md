# lein-hadoop-cluster

A Leiningen plugin for running tasks against a live Hadoop cluster.  Adds a
profile named `plugin.lein-hadoop-cluster/default` which includes the correct
class path etc for the currently-configured Hadoop cluster.

## Usage

Put the following dependency into the `:plugins` vector of your `project.clj`:

```clj
[lein-hadoop-cluster "0.1.5"]
```

Due to technomancy/leiningen#1712, this currently *will not work* as a
profile-level plugin e.g. in your `:user` profile.  It must be a top-level
plugin in each project where you intend to use the plugin.

With the plugin added to the project, you can run tasks under the
`plugin.lein-hadoop-cluster/hadoop-cluster` profile, or otherwise use the
profile to access the live cluster task/JVM configuration.  For example:

    $ lein with-profile plugin.lein-hadoop-cluster/hadoop-cluster trampoline repl

Internally, the plugin defines and makes use of two Leiningen profiles (prefixed
with `plugin.lein-hadoop-cluster/`):

- `plugin.lein-hadoop-cluster/default` – Intended as a replacement for the
  top-level `default`.  By inncludes the profiles `system`, `base`, `dev`,
  `user`, and `plugin.lein-hadoop-cluster/system`.
- `plugin.lein-hadoop-cluster/system` – Additive profile containing the actual
  configuration necessary to access the system Hadoop installation.

Additionally, the plugin expects that project files will specify all their
Hadoop-provided dependencies via the `provided` profile.  Placing them in
e.g. `dev` instead will result in them appearing on the cluster profile
classpaths before the system Hadoop, masking your live cluster configuration.

## License

Copyright © 2014-2015 Marshall Bockrath-Vandegrift

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

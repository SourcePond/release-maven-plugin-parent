Changelog
---------

## 2.0.0
 * Fixed #1: delete generated tags if something went wrong
 * Upgraded dependencies
 * Maven 3.3.3 and Java 8 are required now
 * Evaluate version of plugin to use in tests automatically

### 1.0.1

* Implemented proper transfer of system properties to Maven release build
* Use setLocalRepositoryDirectory on invoker request instead of specifying a parameter
* Updated Mockito to 2.0.80-beta

## 1.0.0

* Major refactoring to use JSR-330 internally (needs at least Maven 3.1.0)
* Preparations to support more SCMs like Subversion (work in progress, currently only Git supported)
* Support for managed dependencies (dependencyManagemend element)
* Support for managed plugins (pluginManagement element)
* Support for dependency/plugin version substitution (versions specified as properties)
* Mechanism to increment snapshot versions of released modules like the maven-release-plugin (see usage page for further information)

# Before fork

### 1.4.2

* Fixed some bugs around change detection in modules where sometimes changes were not being detected.

### 1.4.1

* Added `forceRelease` option to allow forcing modules to be released even if no changes are detected.

## 1.4.0

* New feature: run `releaser:next` to see which versions will be used in the next release.

### 1.3.4

* Fixed bug where a partial build failure where a single commit has multiple tags could result in subsequent releases
failing due to the plugin picking the older tag to use when it is detected that the module hadn't changed. 

## 1.3.0

* Added ssh-agent support thanks to [pull request 7](https://github.com/danielflower/release-maven-plugin/pull/7)

### 1.2.4

* Temporarily reverted version 1.2.2 as it broke compatibility with JDK 6

### 1.2.2

* Fixed bug where the plugin would complain about symlinks (by upgrading the jgit version).

## 1.2.0

* If a parent module changes, then all child modules are updated. This covers cases where upgrading a dependency in a parent
should force all children to be updated.

## 1.1.0

* Bug fix: tags are now pushed before building so that in the event of failure, the next build will use an incremented build number. 
This is needed for cases where part of the build succeeded and some module(s) were uploaded to Nexus - re-uploading would cause an 
error if the build number is not incremented. 

#### 1.0.2

* Bug fix: When a git repository is partially checked out and the report repo has tags that the local repo does not, it was possible that the
generated version number would clash with an existing tag.

#### 1.0.1

* Feature: A list of `releaseProfiles` can now be set in the plugin config.
* Bug fix: Support cases where Windows would return `C:\` and `c:\` for different calls in some situations, causing no sub-modules to build.
* Bug fix: When one commit has multiple tags, the wrong one would sometimes be seen as the latest and cause the release to fail.

## 1.0.0

First stable release.
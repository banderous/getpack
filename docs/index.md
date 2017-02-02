### What is GetPack?

GetPack makes it easier to share, reuse and update Unity plugins.



<iframe width="560" height="315" src="https://www.youtube.com/embed/gw4GXONRhXI" frameborder="0" allowfullscreen></iframe>
&nbsp;

### Dependency management

Unity developers use plugins to avoid reinventing the wheel and reuse great work,
but it's much harder for Unity plugin makers to do the same thing.

Unity plugins are forced to bundle dependencies, causing plugins to conflict with each other
and <a href="https://www.google.com/webhp?sourceid=chrome-instant&ion=1&espv=2&ie=UTF-8#q=android+support+library+conflict+unity">much pain for everyone</a>.

GetPack uses [Gradle's dependency management](https://docs.gradle.org/current/userguide/dependency_management.html) so plugins declare dependencies instead of bundling them.


### Easy upgrades

Switching between package versions is as easy as changing a version number in a JSON file.

What's more, GetPack will preserve local changes during upgrades where possible, and package authors are able to move and rename files when publishing new versions.

### Unobtrusive

GetPack supports any existing plugin directory structure including multiple root folders.

# Requirements

* Windows/OSX
* [JRE/JDK](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
* [Gradle](https://gradle.org/gradle-download)

Follow [Gradle's installation instructions](https://docs.gradle.org/current/userguide/installation.html) ensuring that it is added to your path and running `gradle` from the command prompt succeeds.

<div class="note unreleased">
  <h5>Work in progress</h5>
  <p>An installer will automate installing GetPack and its dependencies.</p>
</div>

# Getting started

<div class="note warning">
  <h5>Back up your project!</h5>
  <p>You are strongly advised to use an SCM tool like Git or SVN when using GetPack.</p>
</div>

## Applying the plugin

GetPack is implemented as a Gradle plugin that is applied to a Gradle project.

1. Create a text file called `build.gradle` next to your Assets folder.
2. Paste the following into it:

```groovy
plugins {
    id 'com.banderous.getpack' version '0.1'
}
```

## The project manifest

The project manifest is where you declare what plugins your project depends on.

You can install, remove and up/downgrade packages just by editing your manifest and **synchronising**.

The project manifest is found at `gp/project.json`.

<div class="note info">
  <h5>You can copy this sample</h5>
  <p>Try pasting it into your own project manifest and running <code>gpSync</code>.</p>
</div>

```json-doc
{
    "repositories" : ["https://github.com/banderous/getpack-sandbox/tree/master/build/repo"],
    "dependencies" : [
        "darktable:minijson:1.0.0",
        "com.google:android-support:23.0.0"
    ]
}
```

<dl>
  <dt>repositories</dt>
  <dd>A list of one or more package repositories where GetPack should retrieve packages.</dd>
  <dt>dependencies</dt>
  <dd>A list of package identifiers that this project depends on.</dd>
</dl>

**Package identifiers** are made up of the publisher, the name of the package and a version, colon delimited.


## Synchronisation

Packages are installed, uninstalled and upgraded with the `gpSync` task, which **synchronises** your project's files to your project's manifest.

New packages added to your manifest are installed, removed packages are uninstalled and changed version numbers are up/downgraded.

```shell
gradle gpSync
```

<div class="note info">
  <p>All GetPack tasks automatically find and launch the correct version of the Unity Editor for your project if it isn't already open.</p>
</div>

### Package installation

New packages are installed when added to the project manifest. GetPack uses Unity's `.unitypackage` format for packaging Assets, so Asset GUIDs and metadata are preserved.

### Package uninstallation

GetPack uninstalls a package if it is removed from the project manifest.

The uninstallation process removes a package's files from the project **except where those files have been modified**.


### Package upgrades

GetPack upgrades a package when you've changed its version number in your project manifest.

The upgrade process allows users to keep local changes if desired, and package authors to rename and move files when publishing new versions.

GetPack performs a three-way comparison between the **current** version installed in the project,
the **incoming** version to be installed, and the **local** files as they currently exist in the project.

<dl>
  <dt>Has the user modified the file since installation?</dt>
  <dd>If not, take the incoming file</dd>
  <dt>Has the package author changed the file between current and incoming?</dt>
  <dd>If not, take the local file</dd>
  <dt>User chooses local or incoming file</dt>
</dl>

---

### The shadow manifest

In order to detect when you've changed your project manifest, GetPack maintains another JSON file called the **shadow manifest**, at `gp/project.json.state`.

GetPack updates the shadow config automatically during the synchronisation process and it should not be manually edited.

<div class="note warning">
  <h5>The shadow config should be checked into your SCM</h5>
  <p>(If you check your dependencies into your SCM)</p>
</div>

## Creating packages

Packages are declared in the **publisher configuration** at `gp/publish.json`.

You can generate a template package configuration with the `gpCreatePackage` task:

```shell
gradle gpCreatePackage
```

This will create a new package configuration to be set up as follows:

```json-doc
{
  // Where to publish your packages.
  // Gradle supports many locations Eg. Amazon S3, HTTP, FTP.
  "repositories": ["s3://amazonaws.com/..."],
  // You can declare multiple packages in a single project.
  "packages": [
    {
      "id": "acme:example:1.0.0",
      // Declare the path(s) to be published in this project relative to Assets/
      "roots": [
        "Plugins/Example/**"
      ],
      // Any dependencies required by this package are declared here.
      "dependencies": ["com.foo:anotherplugin:1.0.0"]
    }
  ]
}
```

If you then run `gradle tasks` you will see new publishing related tasks for your package:

```shell
gradle publishAcmeExample
```

Packages can be published individually by name

```shell
gradle publishAcmeExample
```

Of if a project declares multiple packages they can all be published in a single command:

```shell
gradle publish
```

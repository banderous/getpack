# What?

GetPack automates the installing, removing and upgrading of Unity plugins, built as a [Gradle](https://gradle.org/) plugin to bring powerful features including dependency management.

This video provides an overview of managing plugins using GetPack:

<iframe width="560" height="315" src="https://www.youtube.com/embed/gw4GXONRhXI" frameborder="0" allowfullscreen></iframe>

# Why?

GetPack provides a number of features

### Dependency management

GetPack gives you the power of [Gradle's dependency management](https://docs.gradle.org/current/userguide/dependency_management.html) including:

* Transitive dependencies
* Multiple package repositories
* Dependency substitution

### Refactorable packages

Package authors are free to delete, rename and move files when creating new package versions.

### Unobtrusive

GetPack supports any existing plugin directory structure including multiple root folders (though a single root folder is encouraged).

# Getting started

<div class="note warning">
  <h5>Back up your project!</h5>
  <p>You are strongly advised to use an SCM tool like Git or SVN when using GetPack.</p>
</div>

## Applying the plugin

GetPack is a plugin for the [Gradle build system](https://gradle.org); the same build system used in the Android SDK.

You apply the GetPack plugin to your Gradle project by putting the following at the top of your `build.gradle` file in the root folder of your Unity project.

```groovy
plugins {
    id 'com.banderous.getpack' version '0.1'
}
```

## The project manifest

The project manifest lists the plugins your project depends on, and GetPack uses it during **synchronisation** to decide what to install, uninstall and upgrade.

The manifest is a JSON file found at `gp/ProjectConfig.json` containing two fields.

<div class="note info">
  <h5>You can copy this sample</h5>
  <p>Try pasting this sample into your own project manifest and synchronising.</p>
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

### repositories

A list of one or more package repositories where GetPack should retrieve packages.

### dependencies

A list of package identifiers that this project depends on.

Package identifiers are made up of the publisher, the name of the package and a version, colon delimited.



### Running GetPack

Packages are installed, uninstalled and upgraded during the synchronisation process.

This process ensures that the dependencies installed in your project manifest


This process detects changes to your project manifest and applies them automatically:

```shell
gradle gpSync
```

### Creating packages

Packages are declared in the **publisher configuration** at gp/PublishConfig.json. You can generate a template package configuration with the gpCreatePackage task:

```shell
gradle gpCreatePackage
```

A package has one or more root folders specified as [Ant includes](https://ant.apache.org/manual/dirtasks.html).


```json-doc
// PublishConfig.json
{
  // Packages can be published to any of Gradle's supported formats.
  // Eg. Amazon S3, HTTP, FTP.
  "repositories": ["s3://amazonaws.com/..."],
  "packages": {
    "acme:superjson:1.2.0" : {
        // Declare the path(s) to be published in this project relative to Assets/.
        "roots": [
            "Acme/Superjson/**"
        ],
        // Any dependencies required by this package are declared here.
        "dependencies": ["com.foo:anotherplugin:1.0.0"]
    }
  }
}
```

Packages can be published individually by name

```shell
gradle publishAcmeSuperjson
```

Of if a project declares multiple packages they can all be published in a single command:

```shell
gradle publish
```

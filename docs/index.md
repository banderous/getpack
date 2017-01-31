<iframe width="560" height="315" src="https://www.youtube.com/embed/mMxtlVLgDkI" frameborder="0" allowfullscreen></iframe>

## Why GetPack?


### Dependency management

GetPack gives you the power of [Gradle's dependency management](https://docs.gradle.org/current/userguide/dependency_management.html) including:

* Transitive dependencies
* Multiple package repositories
* Dependency substitution

<div class="note warning">
  <h5>Back up your project!</h5>
  <p>
  You must use an SCM tool like Git or SVN when using GetPack, we will not be responsible for any loss of work arising from use or misuse of GetPack!
  </p>
</div>

### Refactorable packages

Package authors are free to delete, rename and move files when creating new package versions.

### Unobtrusive

GetPack supports any existing plugin directory structure including multiple root folders (though a single root folder is encouraged).

## Getting started

Apply the GetPack Gradle plugin to your build.gradle file.

```groovy
plugins {
    id 'com.banderous.getpack' version '0.1'
}
```


### Declaring dependencies

State **what** your project depends on in the **project manifest**:

```json-doc
// gp/ProjectConfig.json
{
    "repositories" : ["https://github.com/a_repo"],
    "dependencies" : [
        "com.darktable:minijson:1.0.0",
        "com.google:android-support:23.0.0"
    ]
}
```

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

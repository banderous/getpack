<iframe width="560" height="315" src="https://www.youtube.com/embed/mMxtlVLgDkI" frameborder="0" allowfullscreen></iframe>

## Features

### Dependency management


GetPack gives you the full power of [Gradle's dependency management](https://docs.gradle.org/current/userguide/dependency_management.html) including:

* Transitive dependencies
* Multiple package repositories
* Dependency substitution

### Refactorable packages

Package authors are free to delete, rename and move files when creating new package versions.

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
// ProjectConfig.json
{
    "repositories" : ["https://github.com/a_repo"],
    "dependencies" : [
        "com:gaia.thirdparty:1.0.0",
        "com.google:android-support:4.3.1"
    ]
}
```

### Synchronisation

Packages are installed, removed and upgraded automatically by the **synchronisation process**.

This process detects changes to your project manifest and applies them automatically:

```shell
blah sync
```

### Creating packages

Packages are declared in the **publisher configuration**:

```json-doc
// PublishConfig.json
{
  // Packages can be published to any of Gradle's supported formats.
  // Eg. Amazon S3, HTTP, FTP.
  "repositories": ["s3://amazonaws.com/..."],
  "packages": {
    "acme:superjson:1.2.0" : {
        // Declare the path(s) to be published in this project.
        "roots": [
            "Acme/Superjson/**"
        ],
        // Any dependencies required by this package are declared here.
        "dependencies": []
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

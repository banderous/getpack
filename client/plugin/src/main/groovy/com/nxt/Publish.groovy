package com.nxt

import org.gradle.api.Plugin
import org.gradle.api.Project

apply plugin: 'groovy'
apply plugin PublishPlugin

class PublishPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.tasks.create("installPuppet", InstallPuppet.class)
        project.tasks.create("launchUnity", LaunchUnity.class).dependsOn 'installPuppet'
        project.tasks.create("exportPackage", ExportPackage.class).dependsOn 'launchUnity'
        PublishModule.Configure(project)
        InstallPackage.Configure(project)
    }
}

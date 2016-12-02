package com.nxt

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.ivy.IvyPublication
import org.gradle.internal.impldep.aQute.bnd.build.model.clauses.ExportedPackage

apply plugin: 'groovy'
apply plugin PublishPlugin

class PublishPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.tasks.create("installPuppet", InstallPuppet.class)
        project.tasks.create("launchUnity", LaunchUnity.class)
        project.tasks.create("exportPackage", ExportPackage.class).dependsOn 'installPuppet', 'launchUnity'
        PublishModule.Configure(project)
    }
}

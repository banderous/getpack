package com.nxt

import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.publish.ivy.IvyPublication
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.OutputFile

import java.nio.file.Paths

/**
 * Created by alex on 02/12/2016.
 */
class ExportPackage extends DefaultTask {

    enum PathType {
        task("task"),
        export("unitypackage")

        String extension
        PathType(String extension) {
            this.extension = extension
        }
    }

    @OutputFile
    File unityPackage

    Package pack

    public static void Configure(Project project, Config config) {
        project.configurations.create('archives')
        project.pluginManager.apply("ivy-publish")

        project.configure(project) {
            publishing {
                repositories {
                    ivy {
                        // TODO: make configurable
                        url project.file("nxt/repo")
                    }
                }
            }
        }

        config.packages.each { id, pkg ->
            ConfigurePackage(project, pkg)
        }
    }

    private static void ConfigurePackage(Project project, Package pkg) {
        def packageId = "${pkg.group.capitalize()}${pkg.name.capitalize()}"
        def taskName = "nxtExport${packageId}"

        def task = project.tasks.create(taskName, ExportPackage) {
            dependsOn 'launchUnity'
            unityPackage getPath(project, PathType.export, pkg)
            pack pkg
        }

        project.configure(project) {
            publishing {
                publications {
                    "${packageId}"(IvyPublication) {
                        organisation pkg.group
                        module pkg.name
                        artifact (task.unityPackage) {
                            builtBy task
                        }
                    }
                }
            }
        }

    }

    def exportPackageJob(Project project, Package pack) {
        def exportFile = getPath(project, PathType.task, pack)
        exportFile.getParentFile().mkdirs()
        exportFile.createNewFile()


        def builder = new JsonBuilder()
        def baseDir = Paths.get(project.file('.').absolutePath)
        def export = project.fileTree('Assets').files.collect { f ->
            "${baseDir.relativize(f.toPath()).toFile().path}"
        }
        builder.task {
            files export
        }
        exportFile << builder.toString()
    }

    def cleanExistingPackage() {
        if (unityPackage.exists()) {
            unityPackage.delete()
        }
    }

    static File getPath(Project project, PathType type, Package pack) {
        project.file("nxt/${type}/${pack.group}.${pack.name}.${type.extension}")
    }

    @TaskAction
    def action() {
        cleanExistingPackage()
        exportPackageJob(project, pack)
        long startTime = System.currentTimeMillis();
        while(!unityPackage.exists())
        {
            Thread.sleep(100)
            // TODO - sensible timeout
            if (System.currentTimeMillis() - startTime > 5000) {
                throw new GradleException("Timed out waiting for export of ${unityPackage.path}")
            }
        }
    }
}

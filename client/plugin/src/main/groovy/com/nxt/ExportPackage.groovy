package com.nxt

import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.OutputFile

import java.nio.file.Paths

/**
 * Created by alex on 02/12/2016.
 */
class ExportPackage extends DefaultTask {

    public static String TASK_PATH = 'nxt/tasks/export.task'
    @OutputFile
    public File unityPackage = project.file('nxt/package.unitypackage')

    public static void Configure(Project project) {
        project.task('nxtCreateExportJob') {
            doLast() {
                // TODO: refactor.
                File unityPackage = project.file('nxt/package.unitypackage')
                if (unityPackage.exists()) {
                    unityPackage.delete()
                }

                def exportJob = project.file(TASK_PATH)
                exportJob.getParentFile().mkdirs()
                exportJob.createNewFile()

                def builder = new JsonBuilder()
                def baseDir = Paths.get(project.file('.').absolutePath)
                def export = project.fileTree('Assets').files.collect { f ->
                    "${baseDir.relativize(f.toPath()).toFile().path}"
                }
                builder.task {
                    files export
                }
                exportJob << builder.toString()
                println builder.toString()
            }
        }

        project.tasks.create('nxtExportPackage', ExportPackage) {
            dependsOn 'nxtCreateExportJob', 'launchUnity'
        }
    }

    @TaskAction
    def action() {
        long startTime = System.currentTimeMillis();
        while(!unityPackage.exists())
        {
            Thread.sleep(100)
            // TODO - sensible timeout
            if (System.currentTimeMillis() - startTime > 3000) {
                println startTime
                println System.currentTimeMillis()
                throw new GradleException("Timed out waiting for export of ${unityPackage.path}")
            }
        }
    }
}

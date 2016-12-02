package com.nxt

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by alex on 02/12/2016.
 */
class ExportPackage extends  DefaultTask {
    @TaskAction
    def action() {
        def expectedFile = project.file('nxt/package.unitypackage')
        if (expectedFile.exists()) {
            expectedFile.delete()
        }

        def exportJob = project.file('nxt/tasks/export.task')
        exportJob.getParentFile().mkdirs()
        exportJob.createNewFile()
        while (!expectedFile.exists()) {
            Thread.sleep(100)
        }
    }
}

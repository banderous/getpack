package com.nxt

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by alex on 02/12/2016.
 */
class ExportPackage extends  DefaultTask {

    public File unityPackage = project.file('nxt/package.unitypackage')

    @TaskAction
    def action() {
        if (unityPackage.exists()) {
            unityPackage.delete()
        }

        def exportJob = project.file('nxt/tasks/export.task')
        exportJob.getParentFile().mkdirs()
        exportJob.createNewFile()

        // TODO: timeout.
        while (!unityPackage.exists()) {
            Thread.sleep(100)
        }
    }
}

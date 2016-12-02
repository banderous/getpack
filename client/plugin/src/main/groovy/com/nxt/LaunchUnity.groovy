package com.nxt

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by alex on 02/12/2016.
 */
class LaunchUnity extends  DefaultTask {
    @TaskAction
    def action() {
        if (!UnityLauncher.IsUnityRunning(project.file('.'))) {
            def version = UnityLauncher.UnityVersion(project.file('.'))
            def exe = UnityLauncher.UnityExeForVersion(new File('/Applications'), version)
            ProcessBuilder builder = new ProcessBuilder()
            builder.command([
                    exe.path, '-batchmode',
                    '-projectPath', project.file('.').path
            ])
            builder.start()
        }
    }
}

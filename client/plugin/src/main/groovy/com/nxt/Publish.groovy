package com.nxt

import org.gradle.api.Plugin
import org.gradle.api.Project

apply plugin: 'groovy'
apply plugin PublishPlugin

class PublishPlugin implements Plugin<Project> {
    void apply(Project project) {

        project.task("installPuppet") {
            doLast {
                def f = project.file('Assets/Plugins/nxt/Editor/unityPuppet.dll')
                f.getParentFile().mkdirs()
                f.withOutputStream { out ->
                    def i = getClass().getResourceAsStream("/unityPuppet.dll")
                    out << i
                    i.close()
                }
            }
        }

        project.task("launchUnity") {
            if (!UnityLauncher.IsUnityRunning(project.file('.'))) {
                doLast {
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

        project.task("exportPackage", dependsOn: ['installPuppet', 'launchUnity']) {
            doLast {
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
    }
}

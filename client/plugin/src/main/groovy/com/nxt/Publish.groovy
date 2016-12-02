package com.nxt

import org.gradle.api.Plugin
import org.gradle.api.Project

apply plugin: 'groovy'
apply plugin PublishPlugin

class PublishPlugin implements Plugin<Project> {
    void apply(Project project) {

        project.task("installPuppet") {
            doLast {
                def f = project.file('Assets/Plugins/nxt/unityPuppet.dll')
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
    }
}

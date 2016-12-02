package com.nxt

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by alex on 02/12/2016.
 */
class InstallPuppet extends  DefaultTask {
    @TaskAction
    def install() {
        def f = project.file('Assets/Plugins/nxt/Editor/unityPuppet.dll')
        f.getParentFile().mkdirs()
        f.withOutputStream { out ->
            def i = getClass().getResourceAsStream("/unityPuppet.dll")
            out << i
            i.close()
        }
    }
}

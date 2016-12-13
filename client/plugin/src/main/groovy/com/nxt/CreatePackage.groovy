package com.nxt

import com.nxt.config.Config
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Created by alex on 02/12/2016.
 */
class CreatePackage extends DefaultTask {

    public static void Configure(Project project) {
        project.task('nxtCreatePackage') {
            doLast() {
                def c = Config.load(project.file('nxt/nxt.json'))
                def p = project.properties
                c.addPackage p.nxtGroup, p.nxtName, '1.0.0'
            }
        }

    }

    @TaskAction
    def action() {

    }
}

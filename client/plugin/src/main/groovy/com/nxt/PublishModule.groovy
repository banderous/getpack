package com.nxt

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.publish.ivy.IvyPublication
import org.gradle.api.tasks.TaskAction

/**
 * Created by alex on 02/12/2016.
 */
class PublishModule  {
    static void Configure(Project project) {
        project.configurations.create('archives')
        project.pluginManager.apply("ivy-publish")
        project.configure(project) {

            publishing {
                publications {
                    nxtPackage(IvyPublication) {
                        artifact (project.tasks.exportPackage.unityPackage) {
                            builtBy project.tasks.exportPackage
                        }
                    }
                }
                repositories {
                    ivy {
                        url project.file("nxt/repo")
                    }
                }
            }
        }

    }
}

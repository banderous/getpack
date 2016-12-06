package com.nxt

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Copy

/**
 * Created by alex on 02/12/2016.
 */
class InstallPackage {
    public static void Configure(Project project) {
        // We need the default configuration!
        project.pluginManager.apply("base")
        project.configure(project) {
            configurations {
                nxt
            }
            repositories {
                ivy {
                    // TODO: why must we lazy evaluate properties?
                    url {
                        project.properties.nxtRepo
                    }
                }
            }
        }

        project.tasks.create('nxtAddDep') {
            doLast {
                project.configure(project) {
                    dependencies {
                        nxt group: project.properties.nxtGroup, name: project.properties.nxtName, version: project.properties.nxtVersion
                    }
                }
            }
        }

        project.tasks.create('installPackage', Copy.class) { t ->
            from project.configurations.nxt
            into project.file('nxt/import')
            t.dependsOn 'nxtAddDep', 'launchUnity'
        }
    }
}

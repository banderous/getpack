package com.nxt;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import org.gradle.api.Project;
import org.gradle.api.Plugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.publish.ivy.IvyPublication
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.bundling.Zip

import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest;

apply plugin: 'groovy'
apply plugin PublishPlugin

dependencies {
    compile gradleApi()
    compile localGroovy()
}

class PublishPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.configurations.create('archives')
        project.pluginManager.apply("ivy-publish")
        project.configure(project) {
            publishing {
                publications {
                    ivy(IvyPublication) {
                           artifact (exportUnityPackage.output) {
                             builtBy exportUnityPackage
                          }

                        artifact (zipManifest) {
                            builtBy zipManifest
                            name "manifest"
                        }
                        descriptor {
                            extraInfo("outline", "unityVersion", "5.0")
                        }
                    }
                }
                repositories {
                    ivy {
                        url "../build/repo"
                    }
                }
            }

            repositories {
                ivy {
                    url "../build/repo"
                }
            }
        }

        project.task('exportUnityPackage', type: Exec) {
            project.mkdir project.file('build')
            File output = project.file("build/${project.name}.unitypackage")
            ext.output = output
            executable '/Applications/Unity 5.3/Unity.app/Contents/MacOS/Unity'
            args '-batchMode'
            args '-quit'
            args '-projectPath', project.file('.')
            args '-exportPackage', 'Assets'
            args output
            inputs.dir 'Assets'
            inputs.dir 'ProjectSettings'
            outputs.file output
        }

        def foo = project.task('buildManifest') {
            project.ext.manifest = project.file("$project.buildDir/manifest.json")
            doLast {
                JsonBuilder json = new JsonBuilder();
                def tree = project.fileTree('Assets') {
                    exclude "**/*.meta"
                }
                def base = Paths.get(project.file('.').absolutePath)
                json files : tree.collectEntries {
                    ["${base.relativize(it.toPath()).toFile().path}": generateMD5(it)]
                }
                project.ext.manifest.text = json.toString()
            }
        }

        project.task('zipManifest', type: Zip, dependsOn: foo) {
            from project.ext.manifest
        }

        project.task('install') {
            def d = project.configurations.create('target')
            project.dependencies {
                delegate.target("com.outlinegames:MiniJSON:1.0.0")
            }

            doLast {
                project.configurations.target.resolvedConfiguration.getResolvedArtifacts().each { art ->
                    if (art.file.name.endsWith("unitypackage")) {
                        def builder = new ProcessBuilder('/Applications/Unity 5.3/Unity.app/Contents/MacOS/Unity',
                                '-batchMode',
                                '-quit',
                                '-projectPath', project.file('.').path,
                                '-importPackage', art.file.path);
                        builder.start().waitFor();
                    }
                }
            }
//            dependencies {
//                def json = new JsonSlurper().parseText(project.file('manifest.json'))
//                json.packages.each {
//                    target group: it.group, name: it.name, version: it.version
//                }
//            }
        }
    }

    def generateMD5(File f) {
        def digest = MessageDigest.getInstance("MD5")
        f.eachByte(4096) { buffer, length ->
            digest.update(buffer, 0, length)
        }
        return digest.digest().encodeHex() as String
    }
}

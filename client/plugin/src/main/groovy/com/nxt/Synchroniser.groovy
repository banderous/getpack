package com.nxt

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.common.io.Files
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.artifacts.ResolvedDependency

/**
 * Created by alex on 09/12/2016.
 */
class Synchroniser {

    Project project

    public Synchroniser(Project project) {
        this.project = project
    }

    static void Synchronise(Project project) {
        def changes = resolveDeps(project, Config.load(project), Config.loadShadow(project))
        // Copy the new dependencies into place.
        changes.added.each { id, ResolvedDependency dep ->
            dep.allModuleArtifacts.each { art ->
                if (art.file.path.endsWith(".unitypackage")) {
                    Files.copy(art.file, project.file('nxt/import'))
                }
            }
        }
    }

    static Map<String, Map<String, ResolvedDependency>> resolveDeps(Project project,
                                                                    Config projectConfig,
                                                                    Config shadowConfig) {
        def target = resolveConfig(project, projectConfig)
        def currentState = resolveConfig(project, shadowConfig)
        def difference = Maps.difference(mapDependencies(target), mapDependencies(currentState))

        [
                added: difference.entriesOnlyOnLeft(),
                removed: difference.entriesOnlyOnRight(),
                changed: difference.entriesDiffering()
        ]
    }

    static Map<String, ResolvedDependency> mapDependencies(ResolvedConfiguration config) {
        Map<String, ResolvedDependency> result = Maps.newHashMap()
        config.firstLevelModuleDependencies.each { d ->
            result.put([d.moduleGroup, d.moduleName].join(":"), d)
            d.children.each { child ->
                result.putAll(mapDependencies(child))
            }
        }
        result
    }


    static ResolvedConfiguration resolveConfig(Project project, Config config) {
        config.repositories.each {r ->
            project.configure(project) {
                repositories {
                    ivy {
                        url r
                    }
                }
            }
        }


        def gradleConf = project.configurations.create("nxtTemp${count++}")
        config.dependencies.each { s->
            gradleConf.dependencies.add(project.getDependencies().create(s))
        }

        gradleConf.resolvedConfiguration
    }

    static def count = 1

    public static File stateFile(Project project) {
        project.file('nxt/nxt.json.state')
    }
}

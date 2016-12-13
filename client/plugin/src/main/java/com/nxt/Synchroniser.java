package com.nxt;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import com.nxt.config.Config;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;

import java.io.File;
import java.util.Map;
import java.util.Set;

class ResolveResult {
    Map<String, ResolvedDependency> removed;
    Map<String, ResolvedDependency> added;
    Map<String, MapDifference.ValueDifference<ResolvedDependency>> changed;
    public ResolveResult(Map<String, ResolvedDependency> added,
                         Map<String, ResolvedDependency> removed,
                         Map<String, MapDifference.ValueDifference<ResolvedDependency>> changed) {
        this.removed = removed;
        this.added = added;
        this.changed = changed;
    }
}

/**
 * Created by alex on 09/12/2016.
 */
class Synchroniser {

    Project project;

    public Synchroniser(Project project) {
        this.project = project;
    }

    static void Synchronise(Project project) {

    }


    static ResolveResult resolveDeps(Project project,
                                                                    Config projectConfig,
                                                                    Config shadowConfig) {

        ResolvedConfiguration target = resolveConfig(project, projectConfig);
        ResolvedConfiguration currentState = resolveConfig(project, shadowConfig);

        MapDifference<String, ResolvedDependency> difference = Maps.difference(
                mapDependencies(target.getFirstLevelModuleDependencies()),
                mapDependencies(currentState.getFirstLevelModuleDependencies()));


        return new ResolveResult(difference.entriesOnlyOnLeft(),
                difference.entriesOnlyOnRight(),
                difference.entriesDiffering());
    }

    static Map<String, ResolvedDependency> mapDependencies(Set<ResolvedDependency> deps) {
        Map<String, ResolvedDependency> result = Maps.newHashMap();
        for (ResolvedDependency d : deps) {
            String id = Joiner.on(":").join(d.getModuleGroup(), d.getModuleName());
            result.put(id, d);

            for (ResolvedDependency child : d.getChildren()) {
                result.putAll(mapDependencies(child.getChildren()));
            }
        }

        return result;
    }


    static ResolvedConfiguration resolveConfig(Project project, Config config) {
        for (String r : config.getRepositories()) {
            project.getRepositories().ivy(new Action<IvyArtifactRepository>() {
                @Override
                public void execute(IvyArtifactRepository ivyArtifactRepository) {
                    ivyArtifactRepository.setUrl(r);
                }
            });
        }

        Configuration conf = project.getConfigurations().create("nxtTemp" + count++);
        for (String id : config.getDependencies()) {
            conf.getDependencies().add(project.getDependencies().create(id));
        }

        return conf.getResolvedConfiguration();
    }

    static int count = 1;

    public static File stateFile(Project project) {
        return project.file("nxt/nxt.json.state");
    }
}

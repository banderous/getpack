/**
 * Created by alex on 07/12/2016.
 */
package com.nxt.config;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.gradle.api.Project;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class ProjectConfig {

    private final static String CONFIG_PATH = "nxt/nxt.json";
    private final static String SHADOW_CONFIG_PATH = "nxt/nxt.json.state";
    Set<String> repositories = Sets.newHashSet();
    Set<String> dependencies = Sets.newHashSet();

    ProjectConfig() {

    }

    public void clearDependencies() {
        dependencies.clear();
    }

    void addRepository(String url) {
        repositories.add(url);
    }

    public ImmutableSet<String> getRepositories() {
        return ImmutableSet.copyOf(repositories);
    }

    void addDependency(String id) {
        dependencies.add(id);
    }

    public ImmutableSet<String> getDependencies() {
        return ImmutableSet.copyOf(dependencies);
    }

    public static ProjectConfig load(Project project) {
        return load(project.file(CONFIG_PATH));
    }

    public static ProjectConfig loadShadow(Project project) {
        return load(project.file(SHADOW_CONFIG_PATH));
    }

    public static void UpdateShadowWithConfig(Project project) {
        Path source = project.file(CONFIG_PATH).toPath();
        Path dest = project.file(SHADOW_CONFIG_PATH).toPath();
        try {
            java.nio.file.Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static ProjectConfig load(File f) {
        return Util.LoadJSONClass(f, ProjectConfig.class);
    }

    static void save(ProjectConfig config, File f) {
        Util.save(config, f);
    }
}

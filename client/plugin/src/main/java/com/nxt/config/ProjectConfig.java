/**
 * Created by alex on 07/12/2016.
 */

package com.nxt.config;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class ProjectConfig {

  public static final String CONFIG_PATH = "upm/project.json";
  public static final String SHADOW_CONFIG_PATH = "upm/project.json.state";
  Set<String> repositories = Sets.newHashSet();
  Set<String> dependencies = Sets.newHashSet();

  ProjectConfig() {

  }

  public static ProjectConfig loadShadow(Project project) {
    return load(project.file(SHADOW_CONFIG_PATH));
  }

  public static void updateShadowWithConfig(Project project) {
    Path source = project.file(CONFIG_PATH).toPath();
    Path dest = project.file(SHADOW_CONFIG_PATH).toPath();
    try {
      java.nio.file.Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static ProjectConfig load(Project project) {
    return load(project.file(CONFIG_PATH));
  }

  static ProjectConfig load(File f) {
    return Util.loadJSONClass(f, ProjectConfig.class);
  }

  static void save(ProjectConfig config, File f) {
    Util.save(config, f);
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
}

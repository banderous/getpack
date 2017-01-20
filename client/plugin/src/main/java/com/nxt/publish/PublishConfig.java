package com.nxt.publish;

import com.google.common.collect.Sets;
import com.nxt.config.Package;
import com.nxt.config.Util;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.util.Set;

/**
 * Created by alex on 01/01/2017.
 */
public class PublishConfig {
  public static final String PUBLISH_CONFIG_PATH = "upm/publish.json";

  private Set<Package> packages = Sets.newHashSet();
  private Set<String> repositories = Sets.newHashSet();

  public static PublishConfig load(Project project) {
    return Util.loadJSONClass(project.file(PUBLISH_CONFIG_PATH), PublishConfig.class);
  }

  public static void save(File f, PublishConfig config) {
    Util.save(config, f);
  }

  public static void save(Project project, PublishConfig config) {
    Util.save(config, project.file(PUBLISH_CONFIG_PATH));
  }

  public Set<String> getRepositories() {
    return repositories;
  }

  public Package addPackage(String id) {
    Package pack = new Package(id);
    if (packages.contains(pack)) {
      throw new GradleException("Package already installed: " + id);
    }
    packages.add(pack);
    pack.getRoots().add("Plugins/" + StringUtils.capitalize(pack.getName()) + "/**");
    return pack;
  }

  public Package findPackage(String id) {
    for (Package p : packages) {
      if (p.key().equals(id)) {
        return p;
      }
    }

    throw new IllegalArgumentException("Package not found: " + id);
  }

  public Set<Package> getPackages() {
    return packages;
  }

  public void removePackage(String id) {
    packages.remove(new Package(id));
  }

  void addRepository(String url) {
    repositories.add(url);
  }
}

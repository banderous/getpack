package com.nxt;

import com.nxt.config.ProjectConfig;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.file.FileTree;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Compression;
import org.gradle.api.tasks.bundling.Tar;
import org.gradle.wrapper.Install;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by alex on 15/12/2016.
 */
public class SyncDeps extends DefaultTask {
  List<FilteredManifest> manifests;
  Configuration config;
  Configuration shadowConfig;

  static void configure(Project project) {
    ProjectConfig projConf = ProjectConfig.load(project);
    ProjectConfig shadowConf = ProjectConfig.loadShadow(project);

    addRepositories(project, projConf.getRepositories());
    addRepositories(project, shadowConf.getRepositories());

    SyncDeps build = project.getTasks().create("gpDo", SyncDeps.class);
    build.dependsOn("launchUnity");
    build.config = createConfig(project, "getpack", projConf.getDependencies());
    build.shadowConfig = createConfig(project, "getpackShadow", shadowConf.getDependencies());

    Task install = project.getTasks().create("gpInstall");
    install.dependsOn(build);
    install.doLast(new Action<Task>() {
      @Override
      public void execute(Task task) {
        Synchroniser.installPackages(project, build.manifests);
      }
    });

    Task sync = project.getTasks().create("gpSync");
    sync.dependsOn(install);
    sync.getInputs().file(project.file(ProjectConfig.CONFIG_PATH));
    sync.doLast(new Action<Task>() {
      @Override
      public void execute(Task task) {
        ProjectConfig.updateShadowWithConfig(project);
      }
    });
  }

  public static Configuration createConfig(Project project, String name, Set<String> dependencies) {
    Configuration conf = project.getConfigurations().create(name);
    for (String id : dependencies) {
      conf.getDependencies().add(project.getDependencies().create(id));
    }

    return conf;
  }

  public static void addRepositories(Project project, Set<String> repositories) {
    for (final String r : repositories) {
      project.getRepositories().ivy(new Action<IvyArtifactRepository>() {
        @Override
        public void execute(IvyArtifactRepository ivyArtifactRepository) {
          ivyArtifactRepository.setUrl(r);
        }
      });
    }
  }

  @TaskAction
  public void sync() {
    manifests = Synchroniser.sync(getProject(), config, shadowConfig);
  }
}

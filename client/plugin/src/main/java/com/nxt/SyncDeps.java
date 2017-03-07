package com.nxt;

import com.nxt.config.ProjectConfig;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileTree;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Compression;
import org.gradle.api.tasks.bundling.Tar;
import org.gradle.wrapper.Install;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by alex on 15/12/2016.
 */
public class SyncDeps extends DefaultTask {
  List<FilteredManifest> manifests;

  static void configure(Project project) {
    SyncDeps build = project.getTasks().create("gpDo", SyncDeps.class);

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

  @TaskAction
  public void sync() {
    manifests = Synchroniser.sync(getProject());
  }
}

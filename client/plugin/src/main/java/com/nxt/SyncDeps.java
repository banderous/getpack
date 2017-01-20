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
import java.util.concurrent.Callable;

/**
 * Created by alex on 15/12/2016.
 */
public class SyncDeps extends DefaultTask {
  public InstallDetails unityFiles;
  FileTree toMerge;

  static void configure(Project project) {
    SyncDeps build = project.getTasks().create("nxtDo", SyncDeps.class);

    Tar tar = project.getTasks().create("nxtTar", Tar.class);
    tar.dependsOn(build);
    tar.from(build.getUnityFiles());
    tar.getOutputs().upToDateWhen(new Spec<Task>() {
      @Override
      public boolean isSatisfiedBy(Task task) {
        return false;
      }
    });

    tar.setDestinationDir(project.file("nxt/import"));
    tar.setBaseName("package");
    tar.setExtension("staged");
    tar.setCompression(Compression.NONE);

    Task install = project.getTasks().create("nxtInstall");
    install.dependsOn(tar);
    install.doLast(new Action<Task>() {
      @Override
      public void execute(Task task) {
        File staged = project.file("nxt/import/package.staged");

        if (staged.exists()) {
          UnityPuppet.installPackage(project, staged);
        }

        for (File file : build.unityFiles.getUnityPackages()) {
          UnityPuppet.installPackage(project, file);
        }
      }
    });

    Task sync = project.getTasks().create("upmSync");
    sync.dependsOn(install);
    sync.getInputs().file(project.file(ProjectConfig.CONFIG_PATH));
    sync.doLast(new Action<Task>() {
      @Override
      public void execute(Task task) {
        ProjectConfig.updateShadowWithConfig(project);
      }
    });
  }

  public Callable<FileTree> getUnityFiles() {
    return new Callable<FileTree>() {
      @Override
      public FileTree call() throws Exception {
        return toMerge;
      }
    };
  }

  @TaskAction
  public void sync() {
    unityFiles = Synchroniser.sync(getProject());
    toMerge = unityFiles.getPartialPackages();
  }
}

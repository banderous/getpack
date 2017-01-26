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
    SyncDeps build = project.getTasks().create("gpDo", SyncDeps.class);
    build.dependsOn("launchUnity");

    Tar tar = project.getTasks().create("gpTar", Tar.class);
    tar.dependsOn(build);
    tar.from(build.getUnityFiles());
    tar.getOutputs().upToDateWhen(new Spec<Task>() {
      @Override
      public boolean isSatisfiedBy(Task task) {
        return false;
      }
    });

    tar.setDestinationDir(project.getBuildDir());
    tar.setBaseName("package");
    tar.setExtension("unitypackage");
    tar.setCompression(Compression.NONE);

    Task install = project.getTasks().create("gpInstall");
    install.dependsOn(tar);
    install.doLast(new Action<Task>() {
      @Override
      public void execute(Task task) {
        for (File file : build.unityFiles.getUnityPackages()) {
          UnityPuppet.installPackage(project, file);
        }

        try {
          FileTree tree = build.getUnityFiles().call();
          if (tree != null && !tree.isEmpty()) {
            File staged = new File(project.getBuildDir(), "package.unitypackage");

            if (staged.exists()) {
              UnityPuppet.installPackage(project, staged);
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
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

package com.nxt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gradle.api.internal.tasks.options.Option;

/**
 * Created by alex on 02/12/2016.
 */
class LaunchUnity extends DefaultTask {

  @Inject
  public LaunchUnity() {
  }

  boolean useBatchMode = true;

  @Option(option = "batchMode",
          description = "Launch Unity in batch mode (default true)")
  public void setBatchMode(String batchMode) {
    this.useBatchMode = Boolean.parseBoolean(batchMode);
  }

  public static void launch(Project project, boolean batchMode) {
    boolean isRunning = UnityLauncher.isUnityRunning(project.getProjectDir());
    Log.L.info("Unity running {} {}", isRunning, project.getProjectDir());
    if (!UnityLauncher.isUnityRunning(project.getProjectDir())) {
      File exe = UnityLauncher.selectEditorForProject(project.getProjectDir());
      Log.L.info("Launching {} for {}", exe, project.getProjectDir());
      ProcessBuilder builder = new ProcessBuilder();
      List<String> commands = Lists.newArrayList(exe.getPath(), "-projectPath",
              project.getProjectDir().getPath());
      if (batchMode) {
        commands.add("-batchmode");
      }
      builder.command(commands);
      try {
        builder.start();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @TaskAction
  public void action() throws IOException {
    launch(getProject(), useBatchMode);
  }
}

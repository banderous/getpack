package com.nxt;

import com.nxt.config.ProjectConfig;
import com.nxt.config.Util;
import com.nxt.publish.CreatePackage;
import com.nxt.publish.ExportPackage;
import com.nxt.publish.PublishConfig;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class PublishPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.setBuildDir("getpack/build");
    Util.assertGradle3Plus(project.getGradle().getGradleVersion());
    project.getTasks().create("installPuppet", InstallPuppet.class);
    project.getTasks().create("launchUnity", LaunchUnity.class).dependsOn("installPuppet");
    PublishConfig config = PublishConfig.load(project);
    ExportPackage.configure(project, config);
    CreatePackage.configure(project);
    SyncDeps.configure(project);
    // Ensure a project config exists.
    ProjectConfig.load(project);
  }
}

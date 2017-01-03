package com.nxt;

import com.nxt.publish.CreatePackage;
import com.nxt.publish.ExportPackage;
import com.nxt.publish.PublishConfig;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class PublishPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().create("installPuppet", InstallPuppet.class);
        project.getTasks().create("launchUnity", LaunchUnity.class).dependsOn("installPuppet");
        PublishConfig config = PublishConfig.load(project);
        ExportPackage.Configure(project, config);
        CreatePackage.Configure(project);
        SyncDeps.Configure(project);
    }
}

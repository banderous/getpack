package com.nxt;

import com.nxt.config.Config;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

//apply plugin: 'groovy'
//        apply plugin PublishPlugin

public class PublishPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        project.getTasks().create("installPuppet", InstallPuppet.class);
        project.getTasks().create("launchUnity", LaunchUnity.class).dependsOn("installPuppet");
        Config config = Config.load(project);
        ExportPackage.Configure(project, config);
        SyncDeps.Configure(project);
    }
}

package com.nxt.publish;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

/**
 * Created by alex on 03/01/2017.
 */
public class CreatePackage {
    public static void Configure(Project project) {


        project.getTasks().create("nxtCreatePackage").doLast(new Action<Task>() {
            @Override
            public void execute(Task task) {
                PublishConfig config = PublishConfig.load(project);
                config.addPackage("com:example:1.0.0");
                PublishConfig.save(project, config);
            }
        });
    }
}

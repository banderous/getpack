package com.nxt;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.nxt.config.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Created by alex on 15/12/2016.
 */
public class SyncDeps extends DefaultTask {
    static void Configure(Project project) {
        project.getTasks().create("nxtSync", SyncDeps.class);
    }

    @TaskAction
    public void Sync() {

    }
}

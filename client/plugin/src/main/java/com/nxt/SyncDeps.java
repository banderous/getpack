package com.nxt;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.nxt.config.*;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Compression;
import org.gradle.api.tasks.bundling.Tar;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by alex on 15/12/2016.
 */
public class SyncDeps extends DefaultTask {
    static void Configure(Project project) {
        SyncDeps build = project.getTasks().create("nxtDo", SyncDeps.class);

        Tar tar = project.getTasks().create("nxtTar", Tar.class);
        tar.dependsOn(build);
        tar.from(build.getUnityFiles());

        tar.setDestinationDir(project.file("nxt/import"));
        tar.setBaseName("package");
        tar.setExtension("staged");
        tar.setCompression(Compression.GZIP);

        Task sync = project.getTasks().create("nxtSync");
        sync.dependsOn(tar);
        sync.doLast(new Action<Task>() {
            @Override
            public void execute(Task task) {
                File staged = project.file("nxt/import/package.staged");

                if (staged.exists()) {
                    UnityPuppet.InstallPackage(project, staged);
                }
            }
        });
    }

    public FileTree unityFiles;

    public Callable<FileTree> getUnityFiles() {
        return new Callable<FileTree>() {
            @Override
            public FileTree call() throws Exception {
                return unityFiles;
            }
        };
    }

    @TaskAction
    public void Sync() {
        unityFiles = Synchroniser.Sync(getProject());
    }
}

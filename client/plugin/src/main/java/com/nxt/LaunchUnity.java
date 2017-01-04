package com.nxt;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * Created by alex on 02/12/2016.
 */
class LaunchUnity extends  DefaultTask {

    @Inject
    public LaunchUnity() {
    }

    @TaskAction
    public void action() throws IOException {
        Launch(getProject());
    }

    public static void Launch(Project project) {
        boolean isRunning = UnityLauncher.IsUnityRunning(project.getProjectDir());
        Log.L.info(String.format("Unity running %s %s", isRunning, project.getProjectDir()));
        if (!UnityLauncher.IsUnityRunning(project.getProjectDir())) {
            String version = UnityLauncher.UnityVersion(project.getProjectDir());
            File exe = UnityLauncher.UnityExeForVersion(new File("/Applications"), version);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(exe.getPath(), "-batchmode", "-projectPath", project.getProjectDir().getPath());
            try {
                builder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

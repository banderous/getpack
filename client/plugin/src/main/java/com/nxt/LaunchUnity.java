package com.nxt;

import org.gradle.api.DefaultTask;
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
        boolean isRunning = UnityLauncher.IsUnityRunning(getProject().getProjectDir());
        Log.L.info(String.format("Unity running %s %s", isRunning, getProject().getProjectDir()));
        if (!UnityLauncher.IsUnityRunning(getProject().getProjectDir())) {
            String version = UnityLauncher.UnityVersion(getProject().getProjectDir());
            File exe = UnityLauncher.UnityExeForVersion(new File("/Applications"), version);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(exe.getPath(), "-batchmode", "-projectPath", getProject().getProjectDir().getPath());
            builder.start();
        }
    }
}

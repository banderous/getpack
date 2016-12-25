package com.nxt;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by alex on 02/12/2016.
 */
class InstallPuppet extends  DefaultTask {
    public static final String PUPPET_PATH = "Assets/Plugins/nxt/Editor/unityPuppet.dll";

    @Inject
    public InstallPuppet() {
    }

    @TaskAction
    public void install() throws IOException {
        Install(getProject());
    }

    private void Install(Project project) throws IOException {
        File f = project.file(PUPPET_PATH);
        Files.createParentDirs(f);
        ByteStreams.copy(getClass().getResourceAsStream("/unityPuppet.dll"), new FileOutputStream(f));
    }
}

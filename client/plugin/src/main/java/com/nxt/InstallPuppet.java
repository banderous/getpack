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
import java.io.UncheckedIOException;

/**
 * Created by alex on 02/12/2016.
 */
class InstallPuppet extends  DefaultTask {
    public static final String PUPPET_PATH = "Assets/Plugins/nxt/Editor/unityPuppet.dll";

    @Inject
    public InstallPuppet() {
    }

    @TaskAction
    public void install() {
        Install(getProject());
    }

    public static void Install(Project project) {
        File f = project.file(PUPPET_PATH);
        if (!f.exists()) {
            try {
                Files.createParentDirs(f);
                ByteStreams.copy(InstallPuppet.class.getResourceAsStream("/unityPuppet.dll"), new FileOutputStream(f));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}

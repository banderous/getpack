package com.nxt;

import com.google.common.io.Files;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

/**
 * Created by alex on 04/01/2017.
 */
public class UnityPuppet {
    public static final String IMPORT_PACKAGE_PATH = "nxt/import/package.unitypackage";

    public static void InstallPackage(Project project, File unitypackage) {
        InstallPuppet.Install(project);
        LaunchUnity.Launch(project);
        File dest = project.file(IMPORT_PACKAGE_PATH);

        try {
            Files.createParentDirs(dest);
            Files.move(unitypackage, dest);
            File completed = project.file(IMPORT_PACKAGE_PATH + ".completed");
            TimeoutTimer timer = new TimeoutTimer(Constants.DEFAULT_TIMEOUT_SECONDS,
                    "Timed out waiting for import of " + completed);
            while(!completed.exists()) {
                try {
                    Thread.sleep(100);
                    Log.L.debug("Waiting for {}", completed);
                    timer.throwIfExceeded();
                } catch (InterruptedException e) { }
            }

            completed.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

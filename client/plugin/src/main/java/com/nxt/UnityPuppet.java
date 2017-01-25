package com.nxt;

import com.google.common.io.Files;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

/**
 * Created by alex on 04/01/2017.
 */
public class UnityPuppet {
  public static final String IMPORT_PACKAGE_PATH = "upm/import";

  public static void installPackage(Project project, File unitypackage) {
    InstallPuppet.install(project);
    LaunchUnity.launch(project, true);
    File dest = project.file(IMPORT_PACKAGE_PATH + "/" + unitypackage.getName());
    File completed = project.file(dest.getPath() + ".completed");

    try {
      Files.createParentDirs(dest);
      Files.copy(unitypackage, dest);

      TimeoutTimer timer = new TimeoutTimer(Constants.DEFAULT_TIMEOUT_SECONDS,
          "Timed out waiting for import of " + completed);
      while (!completed.exists()) {
        try {
          Thread.sleep(100);
          Log.L.debug("Waiting for {}", completed);
          timer.throwIfExceeded();
        } catch (InterruptedException e) {
          // Nothing to do here.
        }
      }
      completed.delete();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

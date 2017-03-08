package com.nxt;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.*;

/**
 * Created by alex on 02/12/2016.
 */
class InstallPuppet extends DefaultTask {
  public static final String PUPPET_PATH = "Assets/Plugins/getpack/Editor/unityPuppet.dll";

  @Inject
  public InstallPuppet() {
  }

  public static void install(Project project) {
    File f = project.file(PUPPET_PATH);

    try (InputStream in = InstallPuppet.class.getResourceAsStream("/unityPuppet.dll")) {
      Log.L.info("Unity puppet installed {}", f.exists());
      if (f.exists()) {
        HashCode currentHash = Files.hash(f, Hashing.md5());
        HashCode newHash = new HashingInputStream(Hashing.md5(), in).hash();
        boolean upToDate = currentHash.equals(newHash);
        Log.L.info("Unity puppet up to date {}", upToDate);
        if (upToDate) {
          return;
        }
      }
      Files.createParentDirs(f);
      try (FileOutputStream o = new FileOutputStream(f)) {
        ByteStreams.copy(in, o);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @TaskAction
  public void install() {
    install(getProject());
  }
}

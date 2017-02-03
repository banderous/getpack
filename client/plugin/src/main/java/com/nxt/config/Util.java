package com.nxt.config;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nxt.Log;
import org.gradle.api.GradleException;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by alex on 01/01/2017.
 */
public class Util {
  public static boolean onOSX() {
    return OperatingSystem.current().isMacOsX();
  }

  public static boolean onWindows() {
    return OperatingSystem.current().isWindows();
  }

  public static <T> T loadJSONClass(File f, Class<T> c) {
    try {
      if (!f.exists()) {
        Files.createParentDirs(f);
        save(c.newInstance(), f);
      }
      try (FileReader reader = new FileReader(f)) {
        return new Gson().fromJson(reader, c);
      }
    } catch (IOException | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> void save(T object, File f) {
    try {
      Files.write(serialize(object), f, Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void assertGradle3Plus(String gradleVersion) {
    if (!(isGradle3Plus(gradleVersion))) {
      Log.L.error("Gradle {} unsupported, minimum is 3", gradleVersion);
      throw new GradleException("Minimum gradle version is 3. Unsupported: " + gradleVersion);
    }
  }

  public static boolean isGradle3Plus(String gradleVersion) {
    String majorVersion = Splitter.on(".").split(gradleVersion).iterator().next();
    return Integer.parseInt(majorVersion) >= 3;
  }

  public static String serialize(Object o) {
    return new GsonBuilder().setPrettyPrinting().create().toJson(o);
  }
}

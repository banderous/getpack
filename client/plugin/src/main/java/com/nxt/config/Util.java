package com.nxt.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        Files.write("{}", f, Charsets.UTF_8);
      }
      try (FileReader reader = new FileReader(f)) {
        return new Gson().fromJson(reader, c);
      }
    } catch (IOException e) {
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

  public static String serialize(Object o) {
    return new GsonBuilder().setPrettyPrinting().create().toJson(o);
  }
}

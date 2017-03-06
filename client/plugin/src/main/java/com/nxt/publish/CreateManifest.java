package com.nxt.publish;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.nxt.Log;
import com.nxt.config.*;
import com.nxt.config.Package;
import java.util.concurrent.Callable;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.TaskAction;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by alex on 06/03/2017.
 */
public class CreateManifest extends DefaultTask {

  public FileTree packageFiles;
  public Package pack;
  public File manifest;

  @TaskAction
  public void generateManifest() throws IOException {
    PackageManifest.save(generateManifest(getProject(), packageFiles, pack), manifest);
  }

  public static PackageManifest generateManifest(Project project, FileTree tree, Package pack) {
    Log.L.debug("Generating manifest for {} files", tree.getFiles().size());
    // Relativize the paths to the project root,
    // so they start 'Assets/...".
    Path baseURL = Paths.get(project.getProjectDir().getPath());

    PackageManifest manifest = new PackageManifest(pack);
    for (File file : tree.getFiles()) {
      if (Files.getFileExtension(file.getName()).equals("meta")) {
        continue;
      }
      String guid = getGUIDForAsset(file);
      String md5 = generateMD5(file);
      Path path = baseURL.relativize(file.toPath());
      manifest.add(guid, path, md5);
    }

    return manifest;
  }

  public static String generateMD5(File f) {
    try {
      HashCode md5 = Files.hash(f, Hashing.md5());
      return md5.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getGUIDForAsset(File asset) {
    return getGUID(new File(asset.getPath() + ".meta"));
  }

  public static String getGUID(File meta) {
    Yaml yaml = new Yaml();
    try {
      Map map = (Map) yaml.load(new FileInputStream(meta));
      return map.get("guid").toString();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}

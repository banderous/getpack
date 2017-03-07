package com.nxt.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Created by alex on 09/12/2016.
 */
public class PackageManifest {

  // Pathname
  private AssetMap files = new AssetMap();
  private Package pack;
  private File unitypackage;

  private PackageManifest() {

  }

  public PackageManifest(Package pack) {
    this.pack = pack;
  }

  public static PackageManifest load(File from) {
    try {
      return new Gson().fromJson(new FileReader(from), PackageManifest.class);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static void save(PackageManifest manifest, File to) {
    try {
      Files.createParentDirs(to);
      Files.write(new Gson().toJson(manifest), to, Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setUnityPackage(File pack) {
    this.unitypackage = pack;
  }

  public void add(String guid, Path path, String md5) {
    files.put(guid, new Asset(FilenameUtils.separatorsToUnix(path.toString()), md5));
  }

  public String getKey() {
    return pack.key();
  }

  @Override
  public boolean equals(Object obj) {
    final PackageManifest other = (PackageManifest) obj;
    if (null == other) {
      return false;
    }

    return Objects.equals(pack.key(), other.pack.key());
  }

  @Override
  public int hashCode() {
    return pack.key().hashCode();
  }

  public String toString() {
    return new Gson().toJson(this);
  }

  public AssetMap getFiles() {
    return files;
  }

  public File getUnitypackage() {
    return unitypackage;
  }
}

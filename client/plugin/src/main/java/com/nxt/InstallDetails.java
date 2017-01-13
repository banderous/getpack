package com.nxt;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.file.FileTree;

import java.io.File;
import java.util.Set;

/**
 * Created by alex on 13/01/2017.
 */
public class InstallDetails {
  private ImmutableSet<File> unityPackages;
  private FileTree partialPackages;

  public InstallDetails(ImmutableSet<File> unityPackages, FileTree partialPackages) {
    this.unityPackages = unityPackages;
    this.partialPackages = partialPackages;
  }

  public ImmutableSet<File> getUnityPackages() {
    return unityPackages;
  }

  public FileTree getPartialPackages() {
    return partialPackages;
  }
}

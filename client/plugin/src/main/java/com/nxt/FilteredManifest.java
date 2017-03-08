package com.nxt;

import com.google.common.collect.ImmutableSet;
import com.nxt.config.PackageManifest;

import java.util.Set;

/**
 * Created by alex on 26/01/2017.
 */
public class FilteredManifest {
  private PackageManifest manifest;
  private ImmutableSet<String> pathsToInclude;

  public FilteredManifest(PackageManifest manifest, ImmutableSet<String> pathsToInclude) {
    this.manifest = manifest;
    this.pathsToInclude = pathsToInclude;
  }

  public PackageManifest getManifest() {
    return manifest;
  }

  public ImmutableSet<String> getPathsToInclude() {
    return pathsToInclude;
  }
}

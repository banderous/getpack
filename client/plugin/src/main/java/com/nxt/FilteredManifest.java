package com.nxt;

import com.nxt.config.PackageManifest;

import java.util.Set;

/**
 * Created by alex on 26/01/2017.
 */
public class FilteredManifest {
  private PackageManifest manifest;
  private Set<String> guidsToInclude;

  public FilteredManifest(PackageManifest manifest, Set<String> guidsToInclude) {
    this.manifest = manifest;
    this.guidsToInclude = guidsToInclude;
  }

  public PackageManifest getManifest() {
    return manifest;
  }

  public Set<String> getGuidsToInclude() {
    return guidsToInclude;
  }
}

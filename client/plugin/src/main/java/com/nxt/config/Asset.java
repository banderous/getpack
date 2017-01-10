package com.nxt.config;

import com.google.common.base.Joiner;

import java.util.Objects;

public class Asset {
  public transient PackageManifest pack;
  private String md5;
  private String path;

  private Asset() {
  }

  Asset(String path, String md5) {
    this.path = path;
    this.md5 = md5;
  }

  public String getMd5() {
    return md5;
  }

  public String getPath() {
    return path;
  }

  @Override
  public boolean equals(Object obj) {
    final Asset other = (Asset) obj;
    if (null == other) {
      return false;
    }

    return Objects.equals(md5, other.md5)
        && Objects.equals(path, other.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, md5);
  }

  @Override
  public String toString() {
    return Joiner.on(":").join(path, md5);
  }
}

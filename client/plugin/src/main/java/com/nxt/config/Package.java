package com.nxt.config;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;

/**
 * Created by alex on 08/12/2016.
 */
public class Package {
  public String id;
  List<String> roots = Lists.newArrayList();
  List<String> dependencies = Lists.newArrayList();

  // Required for serialization.
  Package() {
  }

  public String getGroup() {
    return getComponents().get(0);
  }

  public String getName() {
    return getComponents().get(1);
  }

  public String getVersion() {
    return getComponents().get(2);
  }

  private List<String> getComponents() {
    return Splitter.on(":").splitToList(id);
  }

  public Package(String id) {
    this.id = id;
  }

  public List<String> getRoots() {
    return roots;
  }

  public List<String> getDependencies() {
    return dependencies;
  }

  public String key() {
    return Joiner.on(":").join(getGroup(), getName());
  }

  @Override
  public boolean equals(Object obj) {
    final Package other = (Package) obj;
    if (null == other) {
      return false;
    }

    return Objects.equals(getGroup(), other.getGroup())
        && Objects.equals(getName(), other.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getGroup(), getName());
  }
}

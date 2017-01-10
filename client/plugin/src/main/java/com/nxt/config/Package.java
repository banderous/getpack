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
  public String group;
  public String name;
  public String version;
  List<String> roots = Lists.newArrayList();

  // Required for serialization.
  Package() {
  }

  public Package(String id) {
    List<String> l = Splitter.on(":").splitToList(id);
    group = l.get(0);
    name = l.get(1);
    version = l.get(2);
  }

  public List<String> getRoots() {
    return roots;
  }

  public String key() {
    return Joiner.on(":").join(group, name);
  }

  @Override
  public boolean equals(Object obj) {
    final Package other = (Package) obj;
    if (null == other) {
      return false;
    }

    return Objects.equals(group, other.group)
        && Objects.equals(name, other.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(group, name);
  }
}

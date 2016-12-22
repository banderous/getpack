package com.nxt.config;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by alex on 08/12/2016.
 */
public class Package {
    public String group, name, version;
    List<String> roots = Lists.newArrayList();

    // Required for serialization.
    Package() {
    }

    Package(String id) {
        List<String> l = Splitter.on(":").splitToList(id);
        group = l.get(0);
        name = l.get(1);
        version = l.get(2);
    }

    public List<String> getRoots() {
        return roots;
    }

    String key() {
        return Joiner.on(":").join(group, name);
    }
}

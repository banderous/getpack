/**
 * Created by alex on 07/12/2016.
 */
package com.nxt.config;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import groovy.json.JsonBuilder;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

class PackageMap extends HashMap<String, Package> {

}

public class Config {

    private final static String CONFIG_PATH = "nxt/nxt.json";
    private final static String SHADOW_CONFIG_PATH = "nxt/nxt.json.state";
    PackageMap packages = new PackageMap();
    Set<String> repositories = Sets.newHashSet();
    Set<String> dependencies = Sets.newHashSet();

    Config() {

    }

    public Package findPackage(String id) {
        return packages.get(id);
    }

    public Package addPackage(String group, String name, String version) {
        String id = Joiner.on(":").join(group, name, version);
        return addPackage(id);
    }

    public Package addPackage(String id) {
        Package pack = new Package(id);
        if (packages.containsKey(pack.key())) {
            throw new GradleException("Package ${id} already installed!");
        }
        packages.put(pack.key(), pack);
        return pack;
    }

    public Package removePackage(String id) {
        return packages.remove(new Package(id).key());
    }

    void addRepository(String url) {
        repositories.add(url);
    }

    public ImmutableSet<String> getRepositories() {
        return ImmutableSet.copyOf(repositories);
    }

    void addDependency(String id) {
        dependencies.add(id);
    }

    public ImmutableSet<String> getDependencies() {
        return ImmutableSet.copyOf(dependencies);
    }

    public static Config load(Project project) {
        return load(project.file(CONFIG_PATH));
    }

    public static Config loadShadow(Project project) {
        return load(project.file(SHADOW_CONFIG_PATH));
    }

    static Config load(File f) {
        try {
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                Files.write("{}", f, Charsets.UTF_8);
            }
            return new Gson().fromJson(new FileReader(f), Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void save(Config config, File f) {
        try {
            Files.write(new Gson().toJson(config), f, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.nxt.publish;

import com.google.common.collect.Sets;
import com.nxt.config.*;
import com.nxt.config.Package;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.util.List;
import java.util.Set;

/**
 * Created by alex on 01/01/2017.
 */
public class PublishConfig {
    public static final String PUBLISH_CONFIG_PATH = "nxt/publish.json";

    private PackageMap packages = new PackageMap();
    private Set<String> repositories = Sets.newHashSet();

    public Set<String> getRepositories() {
        return repositories;
    }

    public Package addPackage(String id) {
        Package pack = new Package(id);
        if (packages.containsKey(pack.key())) {
            throw new GradleException("Package ${id} already installed!");
        }
        packages.put(pack.key(), pack);
        return pack;
    }

    public PackageMap getPackages() { return packages; }
    public Package removePackage(String id) {
        return packages.remove(new Package(id).key());
    }

    void addRepository(String url) {
        repositories.add(url);
    }

    public static PublishConfig load(Project project) {
        return Util.LoadJSONClass(project.file(PUBLISH_CONFIG_PATH), PublishConfig.class);
    }
}

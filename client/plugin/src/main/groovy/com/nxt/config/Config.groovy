/**
 * Created by alex on 07/12/2016.
 */
package com.nxt

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.Project
import com.google.gson.Gson

class PackageMap extends HashMap<String, Package> {

}

class Config {

    private final static String CONFIG_PATH = 'nxt/nxt.json'
    PackageMap packages = new PackageMap()


    public Package findPackage(String id) {
        packages[new Package(id).key()]
    }

    public Package addPackage(String group, String name, String version) {
        addPackage("${group}:${name}:${version}")
    }

    public Package addPackage(String id) {
        def pack = new Package(id)
        if (packages[pack.key()]) {
            throw new GradleException("Package ${id} already installed!")
        }
        packages[pack.key()] = pack
    }

    public Package removePackage(String id) {
        packages.remove(new Package(id).key())
    }

    static Config load(Project project) {
        load(project.file(CONFIG_PATH))
    }

    static Config load(File f) {
        if (!f.exists()) {
            f.getParentFile().mkdirs()
            f.createNewFile()
            f << "{}"
        }

        new Gson().fromJson(f.text, Config)
    }

    static void save(Config config, File f) {
        f << new JsonBuilder(config).toPrettyString()
    }
}

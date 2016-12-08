/**
 * Created by alex on 07/12/2016.
 */
package com.nxt

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.GradleException

class Config {


    def packages = [:]


    public Package findPackage(String group, String name) {
        packages[key(group, name)]
    }

    public Package addPackage(String group, String name, String version) {
        if (packages[key(group, name)]) {
            throw new GradleException("Package ${key(group, name)} already installed!")
        }
        packages[key(group, name)] = new Package(group: group, name: name, version: version)
    }

    public Package removePackage(String group, String name) {
        packages.remove(key(group, name))
    }

    String key(String group, String name) {
        "${group}:${name}"
    }

    static Config load(File f) {
        if (!f.exists()) {
            f.getParentFile().mkdirs()
            f.createNewFile()
            f << "{}"
        }
        return (Config) new JsonSlurper().parse(f)
    }

    static void save(Config config, File f) {
        f << JsonOutput.prettyPrint(JsonOutput.toJson(config))
    }
}

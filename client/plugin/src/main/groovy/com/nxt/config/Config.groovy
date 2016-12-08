/**
 * Created by alex on 07/12/2016.
 */
package com.nxt

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.GradleException

class Config {


    def packages = [:]


    public String findPackage(String group, String name) {
        packages[key(group, name)]
    }

    public void addPackage(String group, String name, String version) {
        if (packages[key(group, name)]) {
            throw new GradleException("Package ${key(group, name)} already installed!")
        }
        packages[key(group, name)] = version
    }

    public void removePackage(String group, String name) {
        packages.remove(key(group, name))
    }

    String key(String group, String name) {
        "${group}:${name}"
    }

    static Config load(File f) {
        return (Config) new JsonSlurper().parse(f)
    }

    static void save(Config config, File f) {
        f << JsonOutput.prettyPrint(JsonOutput.toJson(config))
    }
}

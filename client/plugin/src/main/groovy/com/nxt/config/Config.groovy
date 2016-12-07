/**
 * Created by alex on 07/12/2016.
 */
package com.nxt

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.GradleException

class Config {

    private File file;
    def packages = [:]

    Config(File f) {
        this.file = f;
        if (!f.exists()) {
            f.getParentFile().mkdir()
            f.createNewFile()
        }
        if (!f.text.isEmpty()) {
            packages = new JsonSlurper().parse(f)
        }
    }

    public void addPackage(String group, String name, String version) {
        if (packages[key(group, name)]) {
            throw new GradleException("Package ${key(group, name)} already installed!")
        }
        packages[key(group, name)] = version
        save()
    }

    public void removePackage(String group, String name) {
        packages.remove(key(group, name))
    }

    public String findPackage(String group, String name) {
        packages[key(group, name)]
    }

    def save() {
        file << JsonOutput.prettyPrint(JsonOutput.toJson(this))
    }

    String key(String group, String name) {
        "${group}:${name}"
    }
}

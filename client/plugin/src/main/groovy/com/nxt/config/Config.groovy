/**
 * Created by alex on 07/12/2016.
 */
package com.nxt

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.gradle.api.GradleException

class Config {


    def packages = [:]


    public Package findPackage(String id) {
        packages[new Package(id).key()]
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

    static Config load(File f) {
        if (!f.exists()) {
            f.getParentFile().mkdirs()
            f.createNewFile()
            f << "{}"
        }
        return (Config) new JsonSlurper().parse(f)
    }

    static void save(Config config, File f) {
        f << new JsonBuilder(config).toPrettyString()
    }
}

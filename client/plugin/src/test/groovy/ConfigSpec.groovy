package com.nxt

import spock.lang.Specification
import org.gradle.api.GradleException


class ConfigSpec extends Specification {

    def config = new Config()
    def id = "com.acme:superjson:1.0.1"

    def "creating package"() {
        when:
        def pack = new Package(id)

        then:
        pack.key() == "com.acme:superjson"
        pack.version == "1.0.1"
    }

    def "adding package"() {
        when:
        config.addPackage(id)

        def pack = config.findPackage("com.acme:superjson")

        then:
        pack.group == "com.acme"
        pack.name == "superjson"
        pack.version == "1.0.1"
    }

    def "removing a package"() {
        when:
        config.addPackage(id)
        config.removePackage(id)

        then:
        config.findPackage(id) == null
    }

    def "creating a duplicate package"() {
        when:
        config.addPackage(id)
        config.addPackage(id)

        then:
        thrown GradleException
    }

    def "serialising a config"() {
        when:
        def f = File.createTempFile("foo", "bar")
        config.addPackage(id)
        Config.save(config, f)
        def loaded = Config.load(f)

        then:
        loaded.findPackage(id)
    }
}

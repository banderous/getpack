package com.nxt

import com.google.common.io.Files;
import spock.lang.Specification
import spock.lang.PendingFeature
import org.gradle.api.GradleException


class ConfigSpec extends Specification {

    def config = new Config()

    def "adding package"() {
        when:
        config.addPackage("com.acme", "superjson", "1")

        then:
        config.findPackage("com.acme", "superjson") == "1"
    }

    def "removing a package"() {
        when:
        config.addPackage("com.acme", "superjson", "1")
        config.removePackage("com.acme", "superjson")

        then:
        config.findPackage("com.acme", "superjson") == null
    }

    def "creating a duplicate package"() {
        when:
        config.addPackage("com.acme", "superjson", "1")
        config.addPackage("com.acme", "superjson", "2")

        then:
        thrown GradleException
    }

    def "serialising a config"() {
        when:
        def f = File.createTempFile("foo", "bar")
        config.addPackage("com.acme", "superjson", "1")
        Config.save(config, f)
        def loaded = Config.load(f)

        then:
        loaded.findPackage("com.acme", "superjson") == "1"
    }
}

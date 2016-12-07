package com.nxt;
import spock.lang.Specification
import spock.lang.PendingFeature
import org.gradle.api.GradleException

class ConfigSpec extends Specification {

    def configFile = File.createTempFile("config", "json")
    def config = new Config(configFile)

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

    Config loadedConfig() {
        return new Config(configFile)
    }
}

package com.nxt.publish

import com.nxt.config.Util
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import spock.lang.Specification

/**
 * Created by alex on 01/01/2017.
 */
class PublishConfigSpec extends Specification {

    def config = new PublishConfig()

    def "creating a package"() {
        when:
        config.addPackage("com:acme:1.0.0")

        then:
        config.packages.size() == 1
        config.findPackage('com:acme').roots == ['Plugins/Acme/**']
    }

    def "creating a duplicate package"() {
        when:
        config.addPackage("a:b:1.0.0")
        config.addPackage("a:b:2.0.0")

        then:
        thrown GradleException
    }

    def "serializes empty fields"() {
        when:
        def str = Util.serialize(config)
        def result = new JsonSlurper().parseText str
        then:
        result.packages == []
        result.repositories == []
    }
}

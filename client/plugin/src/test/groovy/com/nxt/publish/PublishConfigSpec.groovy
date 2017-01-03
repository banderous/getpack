package com.nxt.publish

import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import com.google.common.io.Files
import com.nxt.config.PackageMap
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
        config.addPackage("a:b:1.0.0")

        then:
        config.packages.size() == 1
        config.findPackage('a:b').roots == ['Assets']
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
        def str = Util.Serialize(config)
        def result = new JsonSlurper().parseText str
        then:
        result.packages == []
        result.repositories == []
    }
}

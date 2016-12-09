package com.nxt;
import com.nxt.ManifestGenerator
import groovy.json.JsonSlurper
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ManifestGenerationSpec extends Specification {

    def "manifest generation"() {

        when:
        def builder = UBuilder.Builder()
        builder.withFile("Assets/Acme/A.txt")
        def project = ProjectBuilder.builder().withProjectDir(builder.projectDir).build()
        def manifest = ManifestGenerator.GenerateManifest(project)

        then:
        manifest.files instanceof Map
        // This is a hash of the filepath.
        manifest.files['Assets/Acme/A.txt'].md5 == "a0c832eb7a4d88e91161ea65e2fda78b"
    }
}

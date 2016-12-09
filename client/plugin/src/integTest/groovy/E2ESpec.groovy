package com.nxt;

import org.gradle.testkit.runner.GradleRunner
import spock.lang.PendingFeature
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import com.nxt.Trouble

class E2ESpec extends BaseE2ESpec {

    def conditions = new PollingConditions(timeout: 5)
    def group = "acme"
    def name = "superjson"
    def version = "1.0.1"
    def packageId = [group, name, version].join(":")

    def "export a package"() {
        when:
        def project = UBuilder.Builder()
                .withPackage(packageId)
                .withArg("nxtExportAcmeSuperjson")
                .build()

        then:
        assert new File(project.projectDir, "nxt/export/acme.superjson.unitypackage").exists()
    }

    def "publish a package"() {
        when:
        def project = UBuilder.Builder()
                .withPackage(packageId)
                .withArg("publishAcmeSuperjsonPublicationToIvyRepository")
                .build()

        then:
        // Ivy repo is org/name/version.
        def expectedPath = "nxt/repo/${group}/${name}/${version}/${name}-${version}.unitypackage"
        new File(project.projectDir, expectedPath).exists()
    }

    @PendingFeature
    def "install a package"() {
        when:
        def repoProject = UBuilder.Builder()
                .withPackage(packageId)
                .withArg("publishAcmeSuperjsonPublicationToIvyRepository")
                .build()

        def consumer = UBuilder.Builder()
                .withRepository("${repoProject.projectDir.path}/nxt/repo")
                .withDependency(packageId)
                .withArg("installPackage")

        println "producer " + repoProject.projectDir
        println "consumer " + consumer.projectDir
        consumer.build()

        // Create a project that references it
        then:
        conditions.within(5) {
            assert new File(consumer.projectDir, consumer.filepathForPackage(packageId)).exists()
        }
    }
}

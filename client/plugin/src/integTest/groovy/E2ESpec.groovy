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

    @Trouble
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
        def modulePath = new File(project.projectDir, "nxt/repo/${group}/${name}/${version}")

        new File(modulePath, "${name}-${version}.unitypackage").exists()
        new File(modulePath, "${name}-${version}.manifest").exists()
    }

    @Trouble
    def "install a package"() {
        when:
        def repoProject = UBuilder.Builder()
                .withPackage(packageId)
                .withArg("publishAcmeSuperjsonPublicationToIvyRepository")
                .build()

        def consumer = UBuilder.Builder()
                .withRepository("${repoProject.projectDir.path}/nxt/repo")
                .withDependency(packageId)
                .withArg("nxtSync")

        consumer.build()

        // Create a runner that references it
        then:
        conditions.within(5) {
            assert new File(consumer.projectDir, IvyBuilder.assetPathForPackage(packageId)).exists()
        }
    }
}

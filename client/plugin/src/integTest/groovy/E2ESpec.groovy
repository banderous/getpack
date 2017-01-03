package com.nxt;

import org.gradle.testkit.runner.GradleRunner
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import com.nxt.Trouble

class E2ESpec extends BaseE2ESpec {

    def conditions = new PollingConditions(timeout: 5)
    @Shared group = "acme"
    @Shared name = "superjson"
    @Shared version = "1.0.0"
    @Shared packageId = [group, name, version].join(":")

    @Shared GradleRunner packageRunner = publishPackage(packageId)

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
        // Ivy repo is org/name/version.
        def modulePath = new File(packageRunner.projectDir, "nxt/repo/${group}/${name}/${version}")

        then:
        new File(modulePath, "${name}-${version}.unitypackage").exists()
        new File(modulePath, "${name}-${version}.manifest").exists()
    }

    @Trouble
    def "install a dependency"() {
        when:
        def consumer = projectConsumingPackage(packageId)
        consumer.build()

        // Create a runner that references it
        then:
        IvyBuilder.isInstalled(consumer.asProject(), packageId)
    }

    def "remove a dependency"() {
        when:
        def consumer = projectConsumingPackage(packageId)
        consumer.clearDependencies()
        consumer.build()

        then:
        conditions.within(5) {
            assert !IvyBuilder.isInstalled(consumer.asProject(), packageId)
        }
    }

    def projectConsumingPackage(String packageId) {
        def result = UBuilder.Builder()
                .withRepository("${packageRunner.projectDir.path}/nxt/repo")
                .withDependency(packageId)
                .withArg("nxtSync")
        result.build()
        conditions.within(5) {
            assert IvyBuilder.isInstalled(result.asProject(), packageId)
        }
        return result;
    }

    def publishPackage(String packageId) {
        UBuilder.Builder()
                .withPackage(packageId)
                .withArg("publishAcmeSuperjsonPublicationToIvyRepository")
                .build()
    }
}

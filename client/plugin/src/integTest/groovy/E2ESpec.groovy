package com.nxt

import com.google.common.collect.ImmutableSet;
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

    @Shared UBuilder packageRunner = publishPackage(packageId)
    def ivyRepo = IvyBuilder.Create()

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

    def "upgrade a dependency"() {
        when:
        def consumer = projectConsumingPackage(packageId)
        consumer.clearDependencies()

        def newVersion = [group, name, "1.1.0"].join(":")
        def n = publishPackage(newVersion)
        consumer.withRepository(n.projectDir.path + "/nxt/repo")

        consumer.withDependency(newVersion)
        consumer.build()

        then:
        conditions.within(5) {
            assert !IvyBuilder.isInstalled(consumer.asProject(), packageId)

            assert IvyBuilder.isInstalled(consumer.asProject(), newVersion)
        }
    }

    def "package with transitive dependencies"() {
        when:
        def withTransitive = SynchroniserSpec.buildTransitivePackage(ivyRepo, 3)
        def result = UBuilder.Builder()
                .withRepository(ivyRepo.dir.path)
                .withDependency(withTransitive)
                .withArg("nxtSync")
        result.build()


        def tree = result.asProject().fileTree('Assets/Com.foo').exclude('**/*.meta')
        def paths = tree.files.collect { it.name }
        def filenames = ImmutableSet.copyOf(paths)
        def expectedNames = (0..3).collect { "Level${it}-1.0.0.txt".toString() }
        then:

        filenames == ImmutableSet.copyOf(expectedNames)
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

    UBuilder publishPackage(String packageId) {
        def result = UBuilder.Builder()
                .withPackage(packageId)
                .withArg("publishAcmeSuperjsonPublicationToIvyRepository")
        result.build()
        result
    }
}

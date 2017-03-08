package com.nxt

import com.google.common.collect.ImmutableSet
import com.nxt.config.Asset
import com.nxt.config.AssetMap
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
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
    @com.nxt.Trouble
    def "export a package"() {
        when:
        def project = UBuilder.Builder()
                .withPackage(packageId)
                .withArg("gpZipAcmeSuperjson")
        project.withFile('Assets/Irrelevant/File.txt')
        project.build()

        def p = project.asProject()
        def pack = p.file("getpack/build/superjson-1.0.0.zip")
        def paths = p.zipTree(pack).collect { f -> f.name }

        then:
        pack.exists()

        paths == ['Superjson-1.0.0.txt', 'Superjson-1.0.0.txt.meta']
    }


    def "publish a package"() {
        when:
        // Ivy repo is org/name/version.
        def modulePath = new File(packageRunner.projectDir, "getpack/repo/${group}/${name}/${version}")

        then:
        new File(modulePath, "${name}-${version}.zip").exists()
        new File(modulePath, "${name}-${version}.manifest").exists()
    }

    def "publish a package with a dependency"() {
        when:
        def userId = 'acme:usesjson:1.0.0'
        def user = UBuilder.Builder().withPackage(userId)
        user.publishConfig.findPackage('acme:usesjson').getDependencies().add(packageId)
        user.saveConfig()

        user.withArg('publishAcmeUsesjsonPublicationToIvyRepository').build()

        def modulePath = user.asProject().file("getpack/repo/acme/usesjson/1.0.0/ivy-1.0.0.xml")
        println modulePath.text
        def ivy = new XmlSlurper().parse(modulePath)
        def dependency = ivy.dependencies.dependency[0]
        then:
        dependency.@org == 'acme'
        dependency.@name == 'superjson'
        dependency.@rev == '1.0.0'
    }

    @com.nxt.Trouble
    def "install a dependency"() {
        when:
        def consumer = projectConsumingPackage(packageId)
        consumer.build()
        def assets = new File(consumer.projectDir, 'Assets')

        then:
        IvyBuilder.isInstalled(consumer.asProject(), packageId)
        !new File(consumer.projectDir, 'Assets/Assets').exists()
    }

    def "remove a dependency"() {
        when:
        def consumer = projectConsumingPackage(packageId)
        consumer.clearDependencies()
        consumer.build()

        then:
        !IvyBuilder.isInstalled(consumer.asProject(), packageId)
        !consumer.asProject().file('Assets/Acme').exists()
    }

    def "upgrade a dependency"() {
        when:
        def consumer = projectConsumingPackage(packageId)
        consumer.clearDependencies()

        def newVersion = [group, name, "1.1.0"].join(":")
        def n = publishPackage(newVersion)
        consumer.withRepository(n.projectDir.path + "/getpack/repo")

        consumer.withDependency(newVersion)
        consumer.build()

        then:
        assert !IvyBuilder.isInstalled(consumer.asProject(), packageId)
        assert IvyBuilder.isInstalled(consumer.asProject(), newVersion)
    }

    def "consume package with transitive dependencies"() {
        when:
        def withTransitive = SynchroniserSpec.buildTransitivePackage(ivyRepo, 10)
        def result = UBuilder.Builder()
                .withRepository(ivyRepo.dir.path)
                .withDependency(withTransitive)
                .withArg("gpSync")
        result.build()


        def tree = result.asProject().fileTree('Assets/Com.foo').exclude('**/*.meta')
        def paths = tree.files.collect { it.name }
        def filenames = ImmutableSet.copyOf(paths)
        def expectedNames = (0..10).collect { "Level${it}-1.0.0.txt".toString() }
        then:

        filenames == ImmutableSet.copyOf(expectedNames)
    }

    def "preserves local changes during upgrade"() {
        when:
        AssetMap universal = new AssetMap()
        def filePath = 'Assets/Universal.txt'
        universal['guid'] = new Asset(filePath, 'md5')
        def pid = 'com:upgrade:1.0.0'
        ivyRepo.withPackage(pid, new String[0], universal)
        def result = UBuilder.Builder()
                .withRepository(ivyRepo.dir.path)
                .withDependency(pid)
                .withArg("gpSync")
        result.build()

        // Define a new package version.
        def newVersion = 'com:upgrade:1.1.0'
        def newPath = 'Assets/Another.txt'
        universal['newguid'] = new Asset(newPath, 'differentmd5')
        ivyRepo.withPackage(newVersion, new String[0], universal)

        // Modify the existing file locally.
        File localFile = result.asProject().file(filePath)
        localFile.text = "Modified"

        result.clearDependencies()
        result.withDependency(newVersion)
        result.build()

        then:
        "Modified" == localFile.text
        result.asProject().file(newPath).exists()
    }

    def projectConsumingPackage(String packageId) {
        def result = UBuilder.Builder()
                .withRepository("${packageRunner.projectDir.path}/getpack/repo")
                .withDependency(packageId)
                .withArg("gpSync")
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

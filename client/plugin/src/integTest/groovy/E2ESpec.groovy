package com.nxt;

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import com.nxt.Trouble
import static SpecHelper.ProjectWithTask
import static SpecHelper.ProjectWithPackage

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

    @Trouble
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

    def "install a package"() {
        when:
        // Create a dummy repo
        GradleRunner repoProject = ProjectWithTask(ProjectType.DummyFile, "publishAcmeSuperjsonPublicationToIvyRepository")

        GradleRunner consumerProject = ProjectWithTask(ProjectType.Empty, "installPackage",
                "-PnxtRepo=${repoProject.projectDir.path}/nxt/repo",
                "-PnxtGroup=acme",
                "-PnxtName=superjson",
                "-PnxtVersion=1.0.0"
        )

        println "producer " + repoProject.projectDir
        println "consumer " + consumerProject.projectDir

        // Create a project that references it
        then:
        conditions.within(5) {
            assert new File(consumerProject.projectDir, SpecHelper.DUMMY_FILE).exists()
        }
    }
}

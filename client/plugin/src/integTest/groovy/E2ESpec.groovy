package com.nxt;

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import com.nxt.Trouble
import static SpecHelper.ProjectWithTask
import static SpecHelper.ProjectWithPackage

class E2ESpec extends BaseE2ESpec {

    def conditions = new PollingConditions(timeout: 5)

    def "puppet installation"() {
        when:
        def project = ProjectWithTask(ProjectType.Empty, "installPuppet")

        then:
        new File(project.projectDir, "Assets/Plugins/nxt/Editor/unityPuppet.dll").exists()
    }

    def "launching Unity"() {
        when:
        def project = ProjectWithTask(ProjectType.Empty, "launchUnity")

        then:
        conditions.within(5) {
            assert new File(project.projectDir, "Temp/UnityLockfile").exists()
        }
    }

//    @Trouble
    def "export a package"() {
        when:
        def runner = ProjectWithTask(ProjectType.DummyFile, "nxtExportAcmeSuperjson")

        then:
        assert new File(runner.projectDir, "nxt/export/acme.superjson.unitypackage").exists()
    }

    def "publish a package"() {
        when:
        GradleRunner runner = ProjectWithTask(ProjectType.DummyFile, "publishAcmeSuperjsonPublicationToIvyRepository")

        then:
        // Ivy repo is org/name/version.
        def name = ProjectType.DummyFile.name
        def group = ProjectType.DummyFile.group
        def expectedPath = "nxt/repo/${group}/${name}/1.0.0/${name}-1.0.0.unitypackage"
        new File(runner.projectDir, expectedPath).exists()
    }

    @Trouble
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

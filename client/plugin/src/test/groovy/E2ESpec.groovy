import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class E2ESpec extends Specification {

    File projectFolder;
    def conditions = new PollingConditions(timeout: 5)

    def cleanupSpec() {
        // Kill all the Unity processes we start.
        "pkill Unity".execute()
    }

    def "puppet installation"() {
        when:
        projectWithTask("installPuppet")

        then:
        new File(projectFolder, "Assets/Plugins/nxt/Editor/unityPuppet.dll").exists()
    }

    def "launching Unity"() {
        when:
        projectWithTask("launchUnity")

        then:
        conditions.within(5) {
            assert new File(projectFolder, "Temp/UnityLockfile").exists()
        }
    }

    def "export a package"() {
        when:
        projectWithTask("exportPackage")

        then:
        assert new File(projectFolder, "nxt/package.unitypackage").exists()
    }

    def "publish a package"() {
        when:
        projectWithTask("publishNxtPublicationToIvyRepository")

        then:
        // Ivy repo is org/name/version.
        def name = projectFolder.name
        def expectedPath = "nxt/repo/nxt/${name}/1.0.0/${name}-1.0.0.unitypackage"
        new File(projectFolder, expectedPath).exists()
    }

    def projectWithTask(task) {
        projectFolder = SpecHelper.dummyProjectFolder()
        println "Test for project " + projectFolder
        GradleRunner.create()
                .withProjectDir(projectFolder)
                .withArguments("-i")
                .withArguments(task)
                .withPluginClasspath()
                .forwardOutput()
                .build()
    }
}

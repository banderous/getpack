import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class E2ESpec extends Specification {

    def projectFolder;
    def conditions = new PollingConditions(timeout: 5)

    def cleanupSpec() {
        // Kill all the Unity processes we start.
        "pkill Unity".execute()
    }

    def "puppet installation"() {
        when:
        projectWithTask("installPuppet")

        then:
        new File(projectFolder, "Assets/Plugins/nxt/unityPuppet.dll").exists()
    }

    def "launching Unity"() {
        when:
        projectWithTask("launchUnity")

        then:
        true
        conditions.within(5) {
            assert new File(projectFolder, "Temp/UnityLockfile").exists()
        }
    }

    def projectWithTask(task) {
        projectFolder = SpecHelper.dummyProjectFolder()
        println projectFolder
        GradleRunner.create()
                .withProjectDir(projectFolder)
                .withArguments('-i', task)
                .withPluginClasspath()
                .forwardOutput()
                .build()
    }
}

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class E2ESpec extends Specification {

    def projectFolder;

    def "puppet installation"() {
        when:
        projectWithTask("installPuppet")

        then:
        new File(projectFolder, "Assets/Plugins/nxt/unityPuppet.dll").exists()
    }

    def projectWithTask(task) {
        projectFolder = SpecHelper.dummyProjectFolder()
        println projectFolder
        GradleRunner.create()
                .withProjectDir(projectFolder)
                .withArguments(task)
                .withPluginClasspath()
                .build()
    }
}

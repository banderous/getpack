import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import com.nxt.Trouble

class E2ESpec extends Specification {

    File projectFolder;
    def conditions = new PollingConditions(timeout: 5)

    def cleanupSpec() {
        // Kill all the Unity processes we start.
        "pkill Unity".execute()
    }

    def "puppet installation"() {
        when:
        projectWithTask(ProjectType.Empty, "installPuppet")

        then:
        new File(projectFolder, "Assets/Plugins/nxt/Editor/unityPuppet.dll").exists()
    }

    def "launching Unity"() {
        when:
        projectWithTask(ProjectType.Empty, "launchUnity")

        then:
        conditions.within(5) {
            assert new File(projectFolder, "Temp/UnityLockfile").exists()
        }
    }

    def "export a package"() {
        when:
        projectWithTask(ProjectType.DummyFile, "exportPackage")

        then:
        assert new File(projectFolder, "nxt/package.unitypackage").exists()
    }

    def "publish a package"() {
        when:
        projectWithTask(ProjectType.DummyFile, "publishNxtPackagePublicationToIvyRepository")

        then:
        // Ivy repo is org/name/version.
        def name = projectFolder.name
        def expectedPath = "nxt/repo/nxt/${name}/1.0.0/${name}-1.0.0.unitypackage"
        new File(projectFolder, expectedPath).exists()
    }

    @Trouble
    def "install a package"() {
        when:
        // Create a dummy repo
        File repoProject = projectWithTask(ProjectType.DummyFile, "publishNxtPackagePublicationToIvyRepository")

        File consumerProject = projectWithTask(ProjectType.Empty, "installPackage",
                "-PnxtRepo=${repoProject.path}/nxt/repo",
                "-PnxtGroup=nxt",
                "-PnxtName=${repoProject.name}",
                "-PnxtVersion=1.0.0"
        )

        println "producer " + repoProject
        println "consumer " + consumerProject

        // Create a project that references it
        then:
        conditions.within(5) {
            assert new File(consumerProject, SpecHelper.DUMMY_FILE).exists()
        }
    }

    def projectWithTask(ProjectType projectType, String task, String[] args = []) {
        projectFolder = SpecHelper.dummyProjectFolder(projectType)
        println "Test for ${task} args ${args} in ${projectFolder}"
        def command = [task, "-i"]
        command.addAll(args)
        GradleRunner r = GradleRunner.create()
                .withProjectDir(projectFolder)
                .withArguments(command)
                .withPluginClasspath()
                .forwardOutput()
        r.build()
        return projectFolder
    }
}

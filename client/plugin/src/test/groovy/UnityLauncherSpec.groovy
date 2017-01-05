import com.google.common.io.Files
import com.nxt.Trouble
import com.nxt.UnityLauncher
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import java.nio.channels.FileLock

class UnityLauncherSpec extends Specification {

    def "detects the last used unity version for a project"() {
        when:
        def versionPath = "src/test/resources/projects/${expectedVersion}"
        def version = UnityLauncher.UnityVersion(new File(versionPath))

        then:
        version == expectedVersion

        where:
        expectedVersion << ["5.0.0p3", "5.3.5f1"]
    }

    def "returns null if no version file exists"() {
        when:
        def version = UnityLauncher.UnityVersion(new File("nonsense"))

        then:
        version == null
    }

    def "finds the installed Editors"() {
        when:
        def searchPath = new File("src/test/resources/Applications")
        def editors = UnityLauncher.FindInstalledEditors(searchPath)

        then:
        editors == [
                "5.0.0p3": new File("src/test/resources/Applications/Unity 5.0.0p3/Unity.app/Contents/MacOS/Unity"),
                "5.3.5f1": new File("src/test/resources/Applications/Unity 5.3.5f1/Unity.app/Contents/MacOS/Unity")
        ]
    }

    def editors = ['5.0.0': new File('a'), '5.0.1': new File('b')]

    def "selects the editor version matching the project"() {
        when:
        def editor = UnityLauncher.SelectEditor(editors, "5.0.1")

        then:
        editor == new File('b')
    }

    def "throws if the projects unity version isn't found"() {
        when:
        UnityLauncher.SelectEditor(editors, "5.0.2")

        then:
        thrown IllegalArgumentException
    }

    def "selects the highest editor version if no project version is specified"() {
        when:
        def editor = UnityLauncher.SelectEditor(editors, null)

        then:
        editor == new File('b')
    }

    @Trouble
    def "detects if Unity is running"() {
        when:

        def versionPath = new File("src/test/resources/projects/${version}")
        def tempDir = Files.createTempDir()
        FileUtils.copyDirectory(versionPath, tempDir)
        def lockPath = "$tempDir/Temp/UnityLockfile"
        FileLock lock
        if (locked) {
            lock = new FileOutputStream(lockPath).getChannel().lock();
        }

        def isRunning = UnityLauncher.IsUnityRunning(tempDir)

        then:
        isRunning == locked

        where:
        version| locked
        "5.0.0p3"| true
        "5.0.0p3"| false
        "5.3.5f1"| false
        "5.3.5f1"| true
    }
}

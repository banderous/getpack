import com.google.common.io.Files
import com.nxt.Trouble
import com.nxt.UnityLauncher
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import java.nio.channels.FileLock

class UnityLauncherSpec extends Specification {

    def "detects the correct unity version"() {

        when:
        def versionPath = "src/test/resources/projects/${expectedVersion}"
        def version = UnityLauncher.UnityVersion(new File(versionPath))

        then:
        version == expectedVersion

        where:
        expectedVersion| _
        "5.0.0p3"| _
        "5.3.5f1"| _
    }

    def "fails if project not found"() {

        when:
        def version = UnityLauncher.UnityVersion(new File("nonsense"))

        then:
        thrown(IllegalArgumentException)
    }

    def "finds the install directory of the specified unity version"() {
        when:
        def searchPath = new File("src/test/resources/Applications")
        def unityPath = UnityLauncher.UnityPathForVersion(searchPath, version)

        then:
        unityPath instanceof File
        unityPath == new File("src/test/resources", expectedPath);

        where:
        version| expectedPath
        "5.0.0p3"| "Applications/Unity 5.0.0p3"
        "5.3.5f1"| "Applications/Unity 5.3.5f1"
    }

    def "throws if unity with version not found"() {
        when:
        def searchPath = new File("src/test/resources/Applications")
        UnityLauncher.UnityPathForVersion(searchPath, "nonsense")

        then:
        thrown(IllegalArgumentException)
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

    def "finds Unity executable for version"() {
        when:
        def executablePath = UnityLauncher.UnityExeForVersion(new File('src/test/resources/Applications'), version)

        then:
        executablePath == answer

        where:
        version| answer
        "5.0.0p3"| new File('src/test/resources/Applications/Unity 5.0.0p3/Unity.app/Contents/MacOS/Unity')
        "5.3.5f1"| new File('src/test/resources/Applications/Unity 5.3.5f1/Unity.app/Contents/MacOS/Unity')
    }
}

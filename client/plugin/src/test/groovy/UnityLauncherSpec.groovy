import com.nxt.UnityLauncher
import spock.lang.Specification

class UnityLauncherSpec extends Specification {

    def "detects the correct unity version"() {

        when:
        def versionPath = "src/test/resources/projects/${expectedVersion}"
        def version = UnityLauncher.UnityVersion(versionPath)

        then:
        version == expectedVersion

        where:
        expectedVersion| _
        "5.0.0p3"| _
        "5.3.5f1"| _
    }

    def "fails if project not found"() {

        when:
        def version = UnityLauncher.UnityVersion("nonsense")

        then:
        thrown(IllegalArgumentException)
    }
}

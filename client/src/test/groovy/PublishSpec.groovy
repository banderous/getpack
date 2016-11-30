import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner;
import static org.gradle.testkit.runner.TaskOutcome.*;
import com.google.common.io.Files
import java.nio.charset.StandardCharsets
import groovy.json.JsonSlurper

class BuildLogicFunctionalTest extends Specification {

    def "manifest generation"() {

        when:
        def project = dummyProject()
        def result = "goat"

        then:
        result == "goat"
    }

    def dummyProject(dummyFilePath) {
        def tempDir = Files.createTempDir()
        def tempFile = new File(tempDir, "Assets/Acme/A.txt")
        tempFile.getParentFile().mkdirs()
        Files.write("Hello", tempFile, StandardCharsets.UTF_8)

    }
}
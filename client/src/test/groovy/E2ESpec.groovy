import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class E2ESpec extends Specification {

    def "manifest generation"() {

        when:
        def result = projectWithTask("buildManifest")

        then:
        result.task(":buildManifest").outcome in [SUCCESS, UP_TO_DATE]
    }

    def projectWithTask(task) {
        GradleRunner.create()
                .withProjectDir(SpecHelper.dummyProjectFolder())
                .withArguments(task)
                .withPluginClasspath()
                .build()
    }
}

import com.nxt.config.Util
import org.gradle.api.GradleException
import spock.lang.Specification

/**
 * Created by alex on 03/02/2017.
 */
class UtilSpec extends Specification {

    def "detects gradle 2"() {
        expect:
        is3Plus == Util.isGradle3Plus(version)

        where:
        version | is3Plus
        '2.14' | false
        '1.1' | false
        '3.0' | true
        '4.0' | true
    }

    def "throws an exception on < gradle 3"() {
        when:
        Util.assertGradle3Plus("2.9.0")

        then:
        thrown(GradleException)
    }
}

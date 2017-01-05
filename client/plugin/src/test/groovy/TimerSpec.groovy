import com.nxt.TimeoutTimer
import org.gradle.api.GradleException
import spock.lang.Specification

/**
 * Created by alex on 05/01/2017.
 */
class TimerSpec extends Specification {
    def "does not throw before the timeout"() {
        when:
        def timer = new TimeoutTimer(100, "Message")

        then:
        timer.throwIfExceeded()
    }

    def "throws after the timeout"() {
        when:
        def timer = new TimeoutTimer(-1, "Message")
        timer.throwIfExceeded()

        then:
        thrown GradleException
    }
}

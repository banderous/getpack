import com.nxt.UBuilder
import spock.lang.PendingFeature
import spock.lang.Specification;

/**
 * Created by alex on 22/12/2016.
 */
public class CreatePackageSpec extends Specification {
    @PendingFeature
    def "creating a new package"() {
        when:
        def project = UBuilder.Builder().asProject()
        project.tasks.nxtCreatePackage.execute()

        then:
        true
    }
}
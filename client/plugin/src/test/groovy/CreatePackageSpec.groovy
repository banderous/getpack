import com.nxt.UBuilder
import com.nxt.publish.PublishConfig
import spock.lang.PendingFeature
import spock.lang.Specification;

/**
 * Created by alex on 22/12/2016.
 */
public class CreatePackageSpec extends Specification {

    def project = UBuilder.Builder().asProject()

    def "with no packages"() {
        when:
        def config = PublishConfig.load(project)

        then:
        config.packages.isEmpty()
    }

    def "creating a new package"() {
        when:
        project.tasks.upmCreatePackage.execute()
        def config = PublishConfig.load(project)
        def pack = config.findPackage('com:example')
        then:
        pack
    }
}

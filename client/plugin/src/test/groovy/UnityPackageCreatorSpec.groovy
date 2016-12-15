import com.nxt.config.Asset
import spock.lang.Specification

import java.nio.file.Paths

/**
 * Created by alex on 15/12/2016.
 */
class UnityPackageCreatorSpec extends Specification {

    def "sources files from the project"() {
//        when:
//        def asset = createAsset("Foo.txt")
    }

    def createAsset(String path) {
        return new Asset(Paths.get(path), path)
    }
}

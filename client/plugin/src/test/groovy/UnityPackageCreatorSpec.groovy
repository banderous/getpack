import com.nxt.IvyBuilder
import com.nxt.Trouble
import com.nxt.config.Asset
import com.nxt.config.AssetMap
import com.nxt.config.PackageManifest
import spock.lang.Specification

import java.nio.file.Paths

/**
 * Created by alex on 15/12/2016.
 */
class UnityPackageCreatorSpec extends Specification {

    def superJSON = "acme:superjson:1.0.0";
    @Trouble
    def "create ivy"() {
        when:
        def repo = IvyBuilder.Create().withPackage(superJSON)

        then:
        true
    }
//   def "sources files from the project"() {
//        Asset asset = new Asset('A.txt', 'md5')
//        asset.pack = manifest;
//
////        when:
////        def asset = createAsset("Foo.txt")
//    }

}

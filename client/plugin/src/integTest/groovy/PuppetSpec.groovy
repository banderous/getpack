import com.google.common.base.Charsets
import com.google.common.collect.ImmutableSet
import com.google.common.hash.Hashing
import com.nxt.BaseE2ESpec
import com.nxt.IvyBuilder
import com.nxt.UBuilder
import com.nxt.UnityPuppet
import com.nxt.config.Asset
import com.nxt.config.AssetMap

/**
 * Created by alex on 04/01/2017.
 */

class PuppetSpec extends BaseE2ESpec {
    def project = UBuilder.Builder()

    def "installs packages"() {
        when:
        def path = 'Assets/A.txt'
        def pack = writeUnityZip([path])
        UnityPuppet.installPackage(project.asProject(), pack, ImmutableSet.of(path))

        def path2 = 'Assets/B.txt'
        pack = writeUnityZip([path2])
        UnityPuppet.installPackage(project.asProject(), pack, ImmutableSet.of(path))

        then:
        project.asProject().file(path).exists()
        // Path2 is not in the include filter.
        !project.asProject().file(path2).exists()
    }

    File writeUnityZip(List<String> paths) {
        def assetMap = new AssetMap()
        paths.each {
            def guid = Hashing.md5().hashString(it, Charsets.UTF_8).toString()
            assetMap.put(guid, new Asset(it, ""))
        }

        IvyBuilder.writeUnityZip(assetMap)
    }
}

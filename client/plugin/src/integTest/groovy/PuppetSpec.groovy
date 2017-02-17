import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.nxt.BaseE2ESpec
import com.nxt.IvyBuilder
import com.nxt.Trouble
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
        def pack = writeUnityPackage([path])
        UnityPuppet.installPackage(project.asProject(), pack)

        def path2 = 'Assets/B.txt'
        pack = writeUnityPackage([path2])
        UnityPuppet.installPackage(project.asProject(), pack)

        then:
        project.asProject().file(path).exists()
        project.asProject().file('gp/build/import').list() == []

        project.asProject().file(path2).exists()
    }

    def writePackage(pack) {
        IvyBuilder.Create().writeUnityPackage(pack)
    }

    File writeUnityPackage(List<String> paths) {
        def assetMap = new AssetMap()
        paths.each {
            def guid = Hashing.md5().hashString(it, Charsets.UTF_8).toString()
            assetMap.put(guid, new Asset(it, ""))
        }

        IvyBuilder.writeUnityPackage(assetMap)
    }
}

import com.google.common.collect.Collections2
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Iterables
import com.google.common.collect.Sets
import com.google.common.io.Files
import com.nxt.CreateTarGZ
import com.nxt.IvyBuilder
import com.nxt.Trouble
import com.nxt.UBuilder
import com.nxt.UnityPackageCreator
import com.nxt.config.Asset
import com.nxt.config.AssetMap
import com.nxt.config.PackageManifest
import org.gradle.api.file.FileTree
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import java.nio.file.Paths

/**
 * Created by alex on 15/12/2016.
 */
class UnityPackageCreatorSpec extends Specification {

    def project = UBuilder.Builder().asProject()

    @Trouble
    def "extracts assets from unitypackages"() {
        when:
        def a = createTarGz("Include/Me.txt", "exclude/notme.txt")
        def b = createTarGz("And/MeToo.txt", "not/meeither.txt")
        def input = ImmutableMap.of(a, Sets.newHashSet("Include"), b, Sets.newHashSet("And"))
        def tree = UnityPackageCreator.DoIt(project, input);
        def names = ImmutableSet.copyOf(tree.files.collect { it.getName() })

        then:
        names == ImmutableSet.of('Me.txt', 'MeToo.txt')
    }

    File createTarGz(String[] files) {
        File folder = Files.createTempDir()
        files.each { filepath ->
            File file = new File(folder, filepath);
            file.getParentFile().mkdirs()
            file << "some contents"
        }

        File archive = File.createTempFile("foo","bar")
        CreateTarGZ.Create(folder, archive)
        archive
    }
}

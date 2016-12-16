import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import com.google.common.io.Files
import com.nxt.CreateTarGZ
import com.nxt.Trouble
import com.nxt.UBuilder
import com.nxt.UnityPackageCreator
import spock.lang.Specification

/**
 * Created by alex on 15/12/2016.
 */
class UnityPackageCreatorSpec extends Specification {

    def project = UBuilder.Builder().asProject()

    def "extracts assets from unitypackages"() {
        when:
        def a = createTarGz("Include/Me.txt", "exclude/notme.txt")
        def b = createTarGz("And/MeToo.txt", "not/meeither.txt")
        HashMultimap<File, String> input = HashMultimap.create()
        input.put(a, "Include")
        input.put(b, "And")
        def tree = UnityPackageCreator.MergeArchives(project, input);
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

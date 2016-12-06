import com.nxt.ManifestGenerator
import groovy.json.JsonSlurper
import spock.lang.Specification

class ManifestGenerationSpec extends Specification {

    def "manifest generation"() {

        when:
        def str = ManifestGenerator.GenerateManifest(SpecHelper.dummyProject(ProjectType.DummyFile))
        def manifest = new JsonSlurper().parseText(str)

        then:
        manifest.files instanceof Map
        // This is a hash of 'Hello'.
        manifest.files["Assets/Acme/A.txt"].md5 == "8b1a9953c4611296a827abf8c47804d7"
    }
}

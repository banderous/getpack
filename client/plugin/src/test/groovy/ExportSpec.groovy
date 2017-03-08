package com.nxt

import com.google.common.collect.ImmutableSet
import com.nxt.publish.CreateManifest
import com.nxt.publish.ExportPackage
import com.nxt.publish.PublishConfig
import org.gradle.testfixtures.ProjectBuilder;
import spock.lang.Specification

class ExportSpec extends Specification {


    def id = 'acme:superjson:1.0.1'

    def "gathers files in package roots"() {
        when:
        def builder = UBuilder.Builder()
        builder.withPackage(id)
        builder.saveConfig()

        def proj = builder.asProject()

        builder.withFile("Assets/Irrelevant.txt")
        builder.withFile("Assets/More/IrrelevantStuff.txt")

        def tree = ExportPackage.gatherForExport(proj, builder.publishConfig.findPackage('acme:superjson'))
        def names = ImmutableSet.copyOf(tree.files.collect { f-> f.name })
        then:
        names == ImmutableSet.of('Superjson-1.0.1.txt', 'Superjson-1.0.1.txt.meta')
    }

    def "manifest generation"() {

        when:
        def builder = UBuilder.Builder()
        def project = builder
                      .withPackage(id)
                      .asProject()

        def pack = builder.publishConfig.packages.first()
        def tree = ExportPackage.gatherForExport(project, pack)
        def manifest = CreateManifest.generateManifest(project, tree, pack)

        then:
        manifest.files instanceof Map
        assert manifest.files.any { guid, info -> (info.path == IvyBuilder.assetPathForPackage(id)
            && info.md5 == "f63671431e05d3286cb0c192e61945e9")}
    }

    def "meta parsing"() {
        when:
        def path = new File('src/test/resources/meta/prefab.meta')
        def meta = CreateManifest.getGUID(path)

        then:
        meta == "4f04e8e06b86e4610af0205cbb62425c"
    }
}

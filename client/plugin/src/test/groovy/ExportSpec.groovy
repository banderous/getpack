package com.nxt

import com.nxt.publish.ExportPackage
import com.nxt.publish.PublishConfig
import org.gradle.testfixtures.ProjectBuilder;
import spock.lang.Specification

class ExportSpec extends Specification {


    def id = 'acme:superjson:1.0.1'

    def "creates export task"() {
        when:
        def project = ProjectBuilder.builder().build()
        def config = new PublishConfig()
        config.addPackage(id)
        ExportPackage.Configure(project, config)

        then:
        project.tasks.nxtExportAcmeSuperjson
    }

    def "gathers files in package roots"() {
        when:
        def builder = UBuilder.Builder()
        builder.withPackage(id)
        builder.saveConfig()


        def proj = builder.asProject()
        proj.tasks.installPuppet.execute()

        builder.withFile("Assets/Irrelevant.txt")
        builder.withFile("Assets/Acme/file.meta")

        def tree = ExportPackage.gatherForExport(proj, builder.publishConfig.findPackage('acme:superjson'))

        then:
        // Ignores puppet dll.
        !tree.files.any { it.path.endsWith(".dll") }
        // Includes our text file
        IvyBuilder.isInstalled(builder.asProject(), id)
        // Ignores irrelevant file.
        !tree.files.any { it.path.endsWith("Irrelevant.txt") }
        // Ignores any meta files.
        !tree.files.any { it.path.endsWith("meta") }
    }

    def "manifest generation"() {

        when:
        def builder = UBuilder.Builder()
        def project = builder
                      .withPackage(id)
                      .asProject()

        def manifest = ExportPackage.GenerateManifest(project, builder.packages.first())

        then:
        manifest.files instanceof Map
        assert manifest.files.any { guid, info -> (info.path == 'Assets/Acme/Superjson-1.0.1.txt'
            && info.md5 == "f63671431e05d3286cb0c192e61945e9")}
    }

    def "meta parsing"() {
        when:
        def path = new File('src/test/resources/meta/prefab.meta')
        def meta = ExportPackage.GetGUID(path)

        then:
        meta == "4f04e8e06b86e4610af0205cbb62425c"
    }
}

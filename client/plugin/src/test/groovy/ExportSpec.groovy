package com.nxt

import org.gradle.api.Project
import org.gradle.internal.impldep.aQute.bnd.annotation.Export
import org.gradle.testfixtures.ProjectBuilder;
import spock.lang.Specification
import org.gradle.api.GradleException

class ExportSpec extends Specification {


    def id = "acme:superjson:1.0.1"

    def "creates export task"() {
        when:
        def project = ProjectBuilder.builder().build()
        def config = new Config()
        config.addPackage(id)
        ExportPackage.Configure(project, config)

        then:
        project.tasks.nxtExportAcmeSuperjson
    }

    def "gathers files in package roots"() {
        when:
        def builder = UBuilder.Builder()
        builder.withPackage(id)
        builder.create()


        def proj = builder.asProject()
        InstallPuppet.Install(proj)

        builder.withFile("Assets/Irrelevant.txt")
        builder.withFile("Assets/Acme/file.meta")

        def tree = ExportPackage.gatherForExport(proj, builder.config.packages['acme:superjson'])

        then:
        // Ignores puppet dll.
        !tree.files.any { it.path.endsWith(".dll") }
        // Includes our text file
        tree.files.any { it.path.endsWith("A.txt")}
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
        assert manifest.files.any { guid, info -> (info.path == 'Assets/Acme/Superjson/A.txt'
            && info.md5 == "944a6d991b9079caee0446d21a2e4770")}
    }

    def "meta parsing"() {
        when:
        def path = new File('src/test/resources/meta/prefab.meta')
        def meta = ExportPackage.GetGUID(path)

        then:
        meta == "4f04e8e06b86e4610af0205cbb62425c"
    }
}

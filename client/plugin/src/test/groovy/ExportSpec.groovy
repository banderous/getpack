package com.nxt

import org.gradle.api.Project
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
        def builder = UBuilder.Builder().withPackage(id).create()

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
        builder.withFile("Assets/Acme/A.txt")
        def project = builder.asProject()
        def tree = project.fileTree("Assets")
        def manifest = ExportPackage.GenerateManifest(project, tree)

        then:
        manifest.files instanceof Map
        // This is a hash of the filepath.
        manifest.files['Assets/Acme/A.txt'].md5 == "a0c832eb7a4d88e91161ea65e2fda78b"
    }
}

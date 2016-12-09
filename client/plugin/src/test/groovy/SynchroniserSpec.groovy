package com.nxt
import com.google.common.io.Files
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.PendingFeature
import spock.lang.Specification

/**
 * Created by alex on 09/12/2016.
 */
class SynchroniserSpec extends Specification {
    def deps = []
    def superJSON = "acme:superjson:1.0.0"

    def project = ProjectBuilder.builder().withProjectDir(Files.createTempDir()).build()
    def builder = UBuilder.Builder(project.projectDir)

    def ivyRepo = IvyBuilder.Create().withPackage(superJSON)

    def setup (){
        builder.withRepository(ivyRepo.dir.path)
    }

    def "detects new package"() {
        when:
        builder.withDependency(superJSON)
        def deps = resolve()

        then:
        deps.added.containsKey 'acme:superjson'
    }

    def "detects removed package"() {
        when:
        builder.withInstalledDependency(superJSON)
        def deps = resolve()

        then:
        deps.removed.containsKey 'acme:superjson'
    }

    def "detects changed package"() {
        when:
        def newVer = "acme:superjson:1.0.1"
        ivyRepo.withPackage(newVer)
        // A new version is requested.
        builder.withDependency(newVer)
        // Old one still installed.
        builder.withInstalledDependency(superJSON)

        def deps = resolve()

        then:
        deps.changed.containsKey 'acme:superjson'
    }

    def "installs new packages"() {
        when:
        builder.withDependency(superJSON)
        builder.create()
        Synchroniser.Synchronise(project)

        then:
        project.fileTree('nxt/import').files.size() == 1
    }

    @PendingFeature
    def "removes unchanged files on removal of packages"() {
        when:
        def file = builder.withFile(builder.filepathForPackage(superJSON))
        def manifest = ManifestGenerator.GenerateManifest(project)
        Synchroniser.RemoveDependency(project, manifest)

        then:
        !file.exists()
    }

    def "does not remove changed files on removal of packages"() {
        when:
        def file = builder.withFile(builder.filepathForPackage(superJSON))
        def manifest = ManifestGenerator.GenerateManifest(project)
        Synchroniser.RemoveDependency(project, manifest)

        then:
        file.exists()
    }

    def "does not remove files not in the manifest"() {
    }

    def "does not remove files required by another plugin"() {
    }

    Map<String, Map<String, ResolvedDependency>> resolve() {
        Synchroniser.resolveDeps(project, builder.config, builder.projectState)
    }
}

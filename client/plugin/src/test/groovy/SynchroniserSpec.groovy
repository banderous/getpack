package com.nxt

import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import com.google.common.collect.Sets
import com.google.common.io.Files
import com.nxt.config.Asset
import com.nxt.config.AssetMap
import com.nxt.config.PackageManifest
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.testfixtures.ProjectBuilder
import org.spockframework.compiler.model.Spec
import spock.lang.PendingFeature
import spock.lang.Specification

import java.nio.file.Paths

/**
 * Created by alex on 09/12/2016.
 */
class SynchroniserSpec extends Specification {
    def deps = []
    def superJSON = "acme:superjson:1.0.0"

    def project = ProjectBuilder.builder().withProjectDir(Files.createTempDir()).build()
    def builder = UBuilder.Builder(project.projectDir)

    def ivyRepo = IvyBuilder.Create().withPackage(superJSON)
    def repositories = Sets.newHashSet(ivyRepo.dir.path)

    def setup (){
        builder.withRepository(ivyRepo.dir.path)
    }


    def "resolves a dependency"() {
        when:
        def deps = Synchroniser.gatherDependencies(
                project,
                repositories,
                Sets.newHashSet(superJSON))
        then:
        deps.size() == 1
        with (deps.first()) {
            moduleName == "superjson"
            moduleVersion == "1.0.0"
            moduleGroup == "acme"
        }
    }

    def "resolves transitive dependencies"() {
        when:
        def firstLevel = "com.foo:usesjson:1.0.0"
        ivyRepo.withPackage(firstLevel, superJSON)
        def secondLevel = "com.foo:usesfoo:1.0.0"
        ivyRepo.withPackage(secondLevel, firstLevel)
        def deps = Synchroniser.gatherDependencies(
                project,
                repositories,
                Sets.newHashSet(secondLevel))
        then:
        deps.size() == 3
        deps.any { it.moduleName == "superjson" }
        deps.any { it.moduleName == "usesjson" }
        deps.any { it.moduleName == "usesfoo" }
    }

    def "resolves package manifests"() {
        when:
        def manifests = Synchroniser.gatherManifests(Synchroniser.gatherDependencies(
                project,
                repositories,
                Sets.newHashSet(superJSON)))

        then:
        manifests.size() == 1
        with (manifests.first()) {
            files.size() == 0
        }
    }

    def assetMap(assets) {
        def result = new PackageManifest()
        assets.each { a ->
            def path = a.size > 1 ? a[1] : a[0] + '.txt'
            def hash = a.size > 2 ? a[2] : a[0]
            result.Add(a[0], Paths.get(path), hash)
        }
        result.files
    }

    def "diffs asset maps"() {
        when:
        def old = assetMap([
                ["disappears"],
                ["newPath", 'old.txt'],
                ["newHash", 'newHash.txt'],
                ["unchanged"]])

        def latest = assetMap([
                ['added'],
                ["newPath", 'new.txt'],
                ["newHash", 'newHash.txt', 'differentHash'],
                ["unchanged"]])

        def diff = Synchroniser.difference(old, latest)


        then:
        with (diff) {
            remove.containsKey 'disappears'
            add.containsKey 'added'
            changed.containsKey 'newPath'
            changed.containsKey 'newHash'
            !(changed.containsKey("unchanged"))
        }
    }

//    def "installs new packages"() {
//        when:
//        builder.withDependency(superJSON)
//        builder.create()
//        Synchroniser.Synchronise(project)
//
//        then:
//        project.fileTree('nxt/import').files.size() == 1
//    }
//
//    def "installs a new package"() {
//        def v1 = [
//                (Assets/A.txt): "A"
//        ]
//
//        builder.addSync(v1)
//        project.file(v1.files.first()).exists()
//    }

//    static class RemovePackageSpec extends SynchroniserSpec {
//        def "removes unchanged files"() {
//            def v1 = [
//                    (Assets/A.txt): "A"
//            ]
//
//            builder.addSync(v1)
//            builder.removeSync(v1)
//            !project.file(v1.files.first()).exists()
//        }
//
//        def "does not remove changed files"() {
//            def v1 = [
//                    (Assets/A.txt): "A"
//            ]
//
//            builder.addSync(v1)
//            v1.files.first() << "nonsense"
//            builder.removeSync(v1)
//            project.file(v1.files.first()).exists()
//        }
//
//        def "does not remove files not in the manifest"() {
//            def projectA = {
//                file('')
//            }
//        }
//
//        def "does not remove files required by another plugin"() {
//        }
//    }
//
//    static class UpdatePackageSpec extends SynchroniserSpec {
//        def "incoming file removed"() {
//
//        }
//
//        def "incoming file moved"() {
//            // Delete file.
//        }
//
//        def "incoming file modified"() {
//            // Do nothing.
//        }
//
//        def "incoming file moved & modified"() {
//            // Delete file.
//        }
//    }
//
//
//    // Set of possible actions:
//
//    // Drop it from the unitypackage.
//    // Issue warning
//    // Replace incoming with local in unitypackage.
//    // Do nothing
//    // Delete local file.
//
//    // Simple case - local file is not changed,
//    // no need to worry about losing changes.
//
//    // The local file has been modified from its original;
//    // specify behaviour for all possible incoming file
//    // scenarios when the local file is modified.
//    static class UpdatePackageWithLocalModificationsSpec extends SynchroniserSpec {
//        def "incoming file removed"() {
//            // Issue warning
//        }
//
//        def "incoming file moved"() {
//            // Replace incoming with local.
//        }
//
//        def "incoming file modified"() {
//            // Issue warning.
//        }
//
//        def "incoming file moved & modified"() {
//            // Replace incoming with local, issue warning.
//        }
//    }

}

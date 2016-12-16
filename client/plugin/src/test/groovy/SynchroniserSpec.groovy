package com.nxt

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import com.google.common.io.Files
import com.nxt.config.Asset
import com.nxt.config.AssetMap
import com.nxt.config.Package
import com.nxt.config.PackageManifest
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import java.nio.file.Paths

/**
 * Created by alex on 09/12/2016.
 */
class SynchroniserSpec extends Specification {
    def deps = []
    def superJSON = "acme:superjson:1.0.0"

    def builder = UBuilder.Builder()
    Project project = null;

    def ivyRepo = IvyBuilder.Create().withPackage(superJSON)
    def repositories = Sets.newHashSet(ivyRepo.dir.path)

    def setup (){
        builder.withRepository(ivyRepo.dir.path)
        project = builder.asProject()
    }

    def "manifests settable"() {
        when:
        def manifest = new PackageManifest(new Package(superJSON))
        def same = new PackageManifest(new Package(superJSON))
        def set = Sets.newHashSet(manifest, same)

        then:
        set.size() == 1
    }

    def "resolves a dependency"() {
        when:
        def deps = resolve(superJSON)
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
        def child = buildTransitivePackage(2)
        def deps = resolve(child)
        then:
        deps.size() == 3
        deps.any { it.moduleName == "level0" }
        deps.any { it.moduleName == "level1" }
        deps.any { it.moduleName == "level2" }
    }

    def "resolves package manifests"() {
        when:
        def child = buildTransitivePackage(4)
        def deps = resolve(child)
        def manifests = Synchroniser.gatherManifests(deps)
        then:
        manifests.size() == 5
        with (manifests.first()) {
            files.size() == 1
        }
    }


    def "builds asset map from all manifests"() {
        when:
        def child = buildTransitivePackage(3)
        def manifests = Synchroniser.gatherManifests(resolve(child))
        def assetMap = Synchroniser.buildAssetMap(manifests)

        then:
        assetMap.size() == 4
    }

    AssetMap assetMap(assets) {
        def result = new PackageManifest()
        assets.each { a ->
            def path = a.size > 1 ? a[1] : a[0] + '.txt'
            def hash = a.size > 2 ? a[2] : a[0]

            result.Add(a[0], Paths.get(path), hash)
        }
        result.files
    }

    def noChangesFilter = new IChangedFileFilter() {
        @Override
        boolean hasLocalModifications(Asset a) {
            return false
        }
    }

    def allChangedFilter = new IChangedFileFilter() {
        @Override
        boolean hasLocalModifications(Asset a) {
            return true;
        }
    }

    def old = assetMap([
            ['disappears'],
            ['newPath', 'old.txt'],
            ['newHash', 'newHash.txt'],
            ['allChange'],
            ['unchanged']])

    def latest = assetMap([
            ['added'],
            ['newPath', 'new.txt'],
            ['newHash', 'newHash.txt', 'differentHash'],
            ['allChange', 'changedPath.txt', 'changedHash'],
            ['unchanged']])

    def "difference with no local changes"() {
        when:
        def diff = Synchroniser.difference(old, latest, noChangesFilter)
        then:

        diff.remove == ImmutableSet.of(
            'disappears.txt',
            'allChange.txt'
        )
        diff.moved == ImmutableMap.of("old.txt", "new.txt")

        with (diff.add) {
            // New file should be added.
            containsKey 'added'

            // The file with different contents should be added.
            with (get('newHash')) {
                md5 == "differentHash"
            }

            with(get('allChange')) {
                path == "changedPath.txt"
            }

            // Do nothing to unchanged file.
            !(containsKey("unchanged"))
        }
    }


    def "difference with local changes"() {
        when:
        def diff = Synchroniser.difference(old, latest, allChangedFilter)
        then:
        old.values().each { v ->
            assert !diff.remove.contains(v.getPath())
        }

        with (diff.moved) {
            get('old.txt') == 'new.txt'
            get('allChange.txt') == 'changedPath.txt'
        }

        diff.add.keySet() == ImmutableSet.of('added')
    }


    def "removing old package"() {
        when:
        builder.withInstalledDependency(superJSON)
        def filePath = IvyBuilder.assetPathForPackage(superJSON)
        def expectedFile = project.file(filePath)
        def meta = project.file(filePath + ".meta")
        assert expectedFile.exists()
        assert meta.exists()
        Synchroniser.Sync(project)

        then:
        !expectedFile.exists()
        !meta.exists()
    }

    @Trouble
    def "installing new package"() {
        when:
        builder.withDependency(superJSON)
        Synchroniser.Sync(project)
        def tree = project.tarTree(project.resources.gzip(Synchroniser.IMPORT_PACKAGE_PATH))
        def paths = tree.files.findAll { it.name == "pathname"}.collect { it.text }
        def filenames = ImmutableSet.copyOf(paths)
        then:
        filenames == ImmutableSet.of('Assets/Acme/Superjson.txt')
    }

//    def "difference with local changes"() {
//        when:
//        def diff = Synchroniser.difference(old, latest, allChangedFilter)
//
//        then:
//        with (diff) {
//            // Old file must not be removed.
//            !(remove.containsKey('disappears'))
//            // New file should be added.
//            add.containsKey 'added'
//            // A file whose path has changed should be removed.
//            remove.containsKey 'newPath'
//            // The new pathed file should be added.
//            add.containsKey 'newPath'
//            // The file with different contents should be added.
//            add.containsKey 'newHash'
//            // We don't need to remove old version of new hashed file.
//            !(remove.containsKey('newHash'))
//            // Do nothing to unchanged file.
//            !(add.containsKey("unchanged"))
//            !(remove.containsKey("unchanged"))
//        }
//    }

//    def "excludes for removal files with local changes"() {
//        when:
//        def map = assetMap([
//                ["1", "Assets/file.txt" ]])
//
//        def diff = Synchroniser.difference(map, new AssetMap(), allChangedFilter)
//        then:
//        !diff.remove.containsKey('1')
//    }

//    def "installs new packages"() {
//        when:
//        builder.withDependency(superJSON)
//        builder.saveConfig()
//        Synchroniser.Synchronise(runner)
//
//        then:
//        runner.fileTree('nxt/import').files.size() == 1
//    }
//
//    def "installs a new package"() {
//        def v1 = [
//                (Assets/A.txt): "A"
//        ]
//
//        builder.addSync(v1)
//        runner.file(v1.files.first()).exists()
//    }

//    static class RemovePackageSpec extends SynchroniserSpec {
//        def "removes unchanged files"() {
//            def v1 = [
//                    (Assets/A.txt): "A"
//            ]
//
//            builder.addSync(v1)
//            builder.removeSync(v1)
//            !runner.file(v1.files.first()).exists()
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
//            runner.file(v1.files.first()).exists()
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

    Set<ResolvedDependency> resolve(String forPackage) {
        Synchroniser.gatherDependencies(project, repositories, Sets.newHashSet(forPackage))
    }

    def buildTransitivePackage(int depth) {

        String parent = "com.foo:level0:1.0.0"
        ivyRepo.withPackage(parent)

        (1..depth).each { level ->
            def child = "com.foo:level${level}:1.0.0"
            ivyRepo.withPackage(child, parent)
            parent = child
        }
        parent
    }
}

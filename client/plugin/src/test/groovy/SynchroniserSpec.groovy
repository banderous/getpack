package com.nxt

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import com.nxt.config.Asset
import com.nxt.config.AssetMap
import com.nxt.config.Package
import com.nxt.config.PackageManifest
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import spock.lang.Specification

import java.nio.file.Paths

/**
 * Created by alex on 09/12/2016.
 */
class SynchroniserSpec extends Specification {
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
        def child = buildTransitivePackage(ivyRepo, 2)
        def deps = resolve(child)
        then:
        deps.size() == 3
        deps.any { it.moduleName == "level0" }
        deps.any { it.moduleName == "level1" }
        deps.any { it.moduleName == "level2" }
    }

    def "resolves package manifests"() {
        when:
        def child = buildTransitivePackage(ivyRepo, 4)
        def deps = resolve(child)
        def manifests = Synchroniser.gatherManifests(deps)
        then:
        manifests.size() == 5
        with (manifests.first()) {
            files.size() == 1
        }
    }

    def "builds guid to unitypackage map"() {
        when:
        def manifests = Synchroniser.gatherManifests(resolve(superJSON))
        def guidFileMap = Synchroniser.buildGUIDToUnitypackageMap(manifests)

        then:
        guidFileMap.size() == 1
    }

    def "builds asset map from all manifests"() {
        when:
        def child = buildTransitivePackage(ivyRepo, 3)
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

            result.add(a[0], Paths.get(path), hash)
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


    def "removing old files"() {
        when:
        def files = ImmutableSet.of('Assets/A.txt', 'Assets/A.txt.meta')
        def b = new FileTreeBuilder(project.projectDir)
        files.each { f ->
            b.file(f, 'Contents')
        }

        Synchroniser.remove(project, files)

        then:
        files.every { !project.file(it).exists() }
    }

    def "moving files"() {
        when:
        new FileTreeBuilder(project.projectDir).Assets {
            file('A.txt', "A");
        }

        Synchroniser.move(project, ImmutableMap.of('Assets/A.txt', 'Assets/B.txt'))
        then:
        !project.file('Assets/A.txt').exists()
        project.file('Assets/B.txt').exists()
    }

    def "no changes does not create import package"() {
        when:
        project.tasks.nxtDo.execute()
        project.tasks.nxtSync.execute()
        then:
        !project.file(UnityPuppet.IMPORT_PACKAGE_PATH).exists()
    }

    Set<ResolvedDependency> resolve(String forPackage) {
        Synchroniser.gatherDependencies(project, repositories, Sets.newHashSet(forPackage))
    }

    static String buildTransitivePackage(IvyBuilder ivy, int depth) {

        String parent = "com.foo:level0:1.0.0"
        ivy.withPackage(parent)

        (1..depth).each { level ->
            def child = "com.foo:level${level}:1.0.0"
            ivy.withPackage(child, parent)
            parent = child
        }
        parent
    }
}

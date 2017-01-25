package com.nxt

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.google.common.io.Files
import com.nxt.config.AssetMap
import com.nxt.config.Package
import com.nxt.config.PackageManifest
import groovy.xml.MarkupBuilder
import org.gradle.api.Project

import java.nio.file.Paths

/**
 * Created by alex on 09/12/2016.
 */
class IvyBuilder {
    public static IvyBuilder Create() {
        new IvyBuilder()
    }

    static def parseId(id) {
        String group, name, version
        (group, name, version) = id.tokenize(":")
        [group: group, name: name, version: version]
    }

    public static String assetContentsForPackage(String id) {
        def vals = parseId(id)
        return vals.name
    }

    public static String assetPathForPackage(vals) {
        return assetPathForPackage(vals.group, vals.name, vals.version)
    }

    public static boolean isInstalled(Project project, String packageId) {
        return new File(project.projectDir, assetPathForPackage(packageId)).exists()
    }

    public static String assetPathForPackage(String id) {
        def vals = parseId(id)
        return assetPathForPackage(vals.group, vals.name, vals.version)
    }

    public static String assetPathForPackage(String group, String name, String version) {
        return ['Assets', group.capitalize(), name.capitalize(), name.capitalize() + "-${version}.txt"].join("/")
    }

    File dir = Files.createTempDir()

    IvyBuilder withPackage(String id, String[] deps) {
        def parsed = parseId(id)
        def builder = new FileTreeBuilder(dir)
        def manifest = createManifest(id)
        builder.dir("${parsed.group}/${parsed.name}/${parsed.version}") {
            file("ivy-${parsed.version}.xml", writeIvyModule(deps, parsed))
            file("${parsed.group}.${parsed.name}-${parsed.version}.manifest", manifest.toString())
            file("${parsed.name}-${parsed.version}.unitypackage", writeUnityPackage(manifest.files))
        }


        this
    }

    public static File writeUnityPackage(AssetMap map) {
        File tarDir = Files.createTempDir()

        def builder = new FileTreeBuilder(tarDir)
        map.each { a ->
            builder.dir(a.key) {
                file("asset", "Fake asset")
                file("pathname", a.value.path)
            }
        }

        // Write the unitypackage.
        File unityPackage = File.createTempFile("fake", ".unitypackage")
        CreateTarGZ.create(tarDir, unityPackage);
        return unityPackage
    }

    def createManifest(String id) {
        def m = new PackageManifest(new Package(id));
        def bits = parseId(id)
        def guid = Hashing.md5().hashString(bits.name, Charsets.UTF_8).toString()
        def path = assetPathForPackage(id)
        def contents = new File(path).name
        def hash = Hashing.md5().hashString(contents, Charsets.UTF_8).toString();
        m.add(guid, Paths.get(path), hash)

        return m
    }

    def writeIvyModule(String[] deps, id) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        def manifestName = [id.group, id.name].join(".")
        xml.('ivy-module')(version: "2.0") {
            info(organisation: id.group, module: id.name, revision: id.version, status: 'integration', publication:"20161209071257")
            configurations()
            publications() {
                artifact(name: id.name, type: 'unitypackage', ext: 'unitypackage')
                artifact(name: manifestName, type: 'manifest', ext: 'manifest')
            }
            dependencies() {
                deps.each { d ->
                    def dep = parseId(d)
                    dependency(org: dep.group, name: dep.name, rev: dep.version)
                }
            }
        }
        writer.toString()
    }
}

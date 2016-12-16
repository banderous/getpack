package com.nxt

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import com.google.common.io.Files
import com.nxt.config.Asset
import com.nxt.config.Package
import com.nxt.config.PackageManifest
import groovy.xml.MarkupBuilder
import org.gradle.api.Project

import java.nio.file.Paths

/**
 * Created by alex on 09/12/2016.
 */
class IvyBuilder {
    static IvyBuilder Create() {
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

    public static String assetPathForPackage(String id) {
        def vals = parseId(id)
        return assetPathForPackage(vals.group, vals.name, vals.version)
    }

    public static String assetPathForPackage(String group, String name, String version) {
        return ['Assets', group.capitalize(), name.capitalize() + ".txt"].join("/")
    }

    File dir = Files.createTempDir()

    IvyBuilder withPackage(String id, String[] deps) {
        def parsed = parseId(id)
        def ivyFolder = new File(dir, "${parsed.group}/${parsed.name}/${parsed.version}")
        ivyFolder.mkdirs()


        writeIvyModule(ivyFolder, deps, parsed.group, parsed.name, parsed.version)
        def manifest = writeManifest(ivyFolder, parsed.group, parsed.name, parsed.version)
        writeUnityPackage(ivyFolder, manifest)

        this
    }

    def writeUnityPackage(File ivyFolder, PackageManifest man) {
        File tarDir = Files.createTempDir()

        for (Map.Entry<String, Asset> a : man.files.entrySet()) {
            File assetFolder = new File(tarDir, a.key)
            assetFolder.mkdir()
            File asset = new File(assetFolder, "asset")
            asset << "Fake asset"

            File path = new File(assetFolder, "pathname")
            path << "Assets/${man.pack.name}.txt"
        }


        // Write the unitypackage.
        File unityPackage = new File(ivyFolder, "${man.pack.name}-${man.pack.version}.unitypackage")
        CreateTarGZ.Create(tarDir, unityPackage);
    }

    def writeManifest(File ivyFolder, String group, String name, String version) {
        def manifestName = [group, name].join(".")
        // Write the manifest.
        File manifest = new File(ivyFolder, manifestName + "-${version}.manifest")
        def m = new PackageManifest(new Package([group, name, version].join(':')));
        def guid = Hashing.md5().hashString(name, Charsets.UTF_8).toString()
        def path = assetPathForPackage(group, name, version)
        def contents = new File(path).name
        def hash = Hashing.md5().hashString(contents, Charsets.UTF_8).toString();
        m.Add(guid, Paths.get(path), hash)
        PackageManifest.save(m, manifest)

        return m
    }

    def writeIvyModule(File ivyFolder, String[] deps, String group, String name, String version) {
        def ivy = new File(ivyFolder, "ivy-${version}.xml")
        def xml = new MarkupBuilder(new FileWriter(ivy))
        def manifestName = [group, name].join(".")
        xml.('ivy-module')(version: "2.0") {
            info(organisation: group, module: name, revision: version, status: 'integration', publication:"20161209071257")
            configurations()
            publications() {
                artifact(name: name, type: 'unitypackage', ext: 'unitypackage')
                artifact(name: manifestName, type: 'manifest', ext: 'manifest')
            }
            dependencies() {
                deps.each { d ->
                    def dep = parseId(d)
                    dependency(org: dep.group, name: dep.name, rev: dep.version)
                }
            }
        }
    }
}

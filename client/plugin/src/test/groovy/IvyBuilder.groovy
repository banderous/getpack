package com.nxt

import com.google.common.io.Files
import com.nxt.config.Package
import com.nxt.config.PackageManifest
import groovy.xml.MarkupBuilder

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

    File dir = Files.createTempDir()

    IvyBuilder withPackage(String id, String[] deps) {
        def parsed = parseId(id)
        def ivyFolder = new File(dir, "${parsed.group}/${parsed.name}/${parsed.version}")
        ivyFolder.mkdirs()
        def ivy = new File(ivyFolder, "ivy-${parsed.version}.xml")
        def xml = new MarkupBuilder(new FileWriter(ivy))
        def manifestName = [parsed.group, parsed.name].join(".")
        xml.('ivy-module')(version: "2.0") {
            info(organisation: parsed.group, module: parsed.name, revision: parsed.version, status: 'integration', publication:"20161209071257")
            configurations()
            publications() {
                artifact(name: parsed.name, type: 'unitypackage', ext: 'unitypackage')
                artifact(name: manifestName, type: 'manifest', ext: 'manifest')
            }
            dependencies() {
                deps.each { d ->
                    def dep = parseId(d)
                    dependency(org: dep.group, name: dep.name, rev: dep.version)
                }
            }
        }


        // Write the manifest.
        File manifest = new File(ivyFolder, manifestName + "-${parsed.version}.manifest")
        def m = new PackageManifest(new Package(id));
        m.Add(parsed.name, Paths.get(parsed.name + ".txt"), parsed.name)
        PackageManifest.save(m, manifest)

        // Write the unitypackage.
        File dummyPackage = new File("src/test/resources/unitypackage/dummy.unitypackage")
        File unityPackage = new File(ivyFolder, "${parsed.name}-${parsed.version}.unitypackage")
        Files.copy(dummyPackage, unityPackage)
        this
    }
}

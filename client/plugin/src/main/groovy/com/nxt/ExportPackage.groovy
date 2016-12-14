package com.nxt

import com.nxt.config.Config
import com.nxt.config.Package
import com.nxt.config.PackageManifest
import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.GradleException
import org.gradle.api.file.FileTree
import org.gradle.api.publish.ivy.IvyPublication
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.OutputFile
import org.yaml.snakeyaml.Yaml

import java.nio.file.Paths
import java.security.MessageDigest

/**
 * Created by alex on 02/12/2016.
 */
class ExportPackage extends DefaultTask {

    enum PathType {
        task("task"),
        export("unitypackage"),
        manifest(".manifest")

        String extension
        PathType(String extension) {
            this.extension = extension
        }
    }

    @OutputFile
    File unityPackage

    @OutputFile
    File manifest

    Package pack

    public static void Configure(Project project, Config config) {
        project.configurations.create('archives')
        project.pluginManager.apply("ivy-publish")

        project.configure(project) {
            publishing {
                repositories {
                    ivy {
                        // TODO: make configurable
                        url project.file("nxt/repo")
                    }
                }
            }
        }

        config.packages.each { id, pkg ->
            ConfigurePackage(project, pkg)
        }
    }

    private static void ConfigurePackage(Project project, Package pkg) {
        def packageId = "${pkg.group.capitalize()}${pkg.name.capitalize()}"
        def taskName = "nxtExport${packageId}"

        def task = project.tasks.create(taskName, ExportPackage) {
            dependsOn 'launchUnity'
            unityPackage getPath(project, PathType.export, pkg)
            manifest getPath(project, PathType.manifest, pkg)
            pack pkg
        }

        project.configure(project) {
            publishing {
                publications {
                    "${packageId}"(IvyPublication) {
                        organisation pkg.group
                        module pkg.name
                        revision pkg.version
                        artifact (task.unityPackage) {
                            builtBy task
                        }
                        artifact (task.manifest) {
                            builtBy task
                        }
                    }
                }
            }
        }

    }

    def exportPackageJob(Project project, Package pack) {
        def exportFile = getPath(project, PathType.task, pack)
        exportFile.getParentFile().mkdirs()
        exportFile.createNewFile()


        def builder = new JsonBuilder()
        def baseDir = Paths.get(project.file('.').absolutePath)
        def export = project.fileTree('Assets').files.collect { f ->
            "${baseDir.relativize(f.toPath()).toFile().path}"
        }
        builder.task {
            files export
        }
        exportFile << builder.toString()
    }

    static FileTree gatherForExport(Project project, Package pack) {
        def tree = project.fileTree('Assets') {
            exclude 'Plugins/nxt'
            exclude '**/*.meta'
        }
        if (pack.roots) {
            pack.roots.each { r ->
                println 'adding ' + r
                tree.include r
            }
        }
        tree
    }

    def cleanExistingPackage() {
        if (unityPackage.exists()) {
            unityPackage.delete()
        }
    }

    static File getPath(Project project, PathType type, Package pack) {
        project.file("nxt/${type}/${pack.group}.${pack.name}.${type.extension}")
    }

    public static PackageManifest GenerateManifest(Project project, Package pack) {
        def tree = gatherForExport(project, pack)
        GenerateManifest(project, tree)
    }

    public static PackageManifest GenerateManifest(Project project, FileTree tree) {
        // Relativize the paths to the project root,
        // so they start 'Assets/...".
        def baseURL = Paths.get(project.projectDir.path)
        def manifest = new PackageManifest()
        tree.each { file ->
            def guid = GetGUIDForAsset(file)
            def md5 = generateMD5(file)
            def path = baseURL.relativize(file.toPath())
            manifest.Add(guid, path, md5);
        }
        manifest
    }

    public static String GetGUIDForAsset(File asset) {
        GetGUID(new File(asset.path + ".meta"))
    }

    public static String GetGUID(File meta) {
        def yaml = new Yaml()
        yaml.load(meta.text).guid
    }

    public static String generateMD5(File f) {
        def digest = MessageDigest.getInstance("MD5")
        f.eachByte(4096) { buffer, length ->
            digest.update(buffer, 0, length)
        }
        return digest.digest().encodeHex() as String
    }

    @TaskAction
    def action() {
        PackageManifest.save(GenerateManifest(project, pack), manifest)

        cleanExistingPackage()
        exportPackageJob(project, pack)
        long startTime = System.currentTimeMillis();
        while(!unityPackage.exists())
        {
            Thread.sleep(100)
            // TODO - sensible timeout
            if (System.currentTimeMillis() - startTime > 5000) {
                throw new GradleException("Timed out waiting for export of ${unityPackage.path}")
            }
        }
    }
}

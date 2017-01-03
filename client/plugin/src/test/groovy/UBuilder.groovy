package com.nxt

import com.google.common.base.Charsets
import com.google.common.collect.Lists
import com.google.common.hash.Hashing
import com.google.common.io.Files
import com.nxt.config.Config
import com.nxt.config.Package
import com.nxt.config.Util
import com.nxt.publish.PublishConfig
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.yaml.snakeyaml.Yaml

import java.nio.file.Paths

/**
 * Created by alex on 09/12/2016.
 */
class UBuilder {
    public static UBuilder Builder() {
        new UBuilder()
    }

    public static UBuilder Builder(File f) {
        new UBuilder(f)
    }

    List<Package> packages = Lists.newArrayList()
    File projectDir
    Config config = new Config()
    PublishConfig publishConfig = new PublishConfig()
    Config projectState = new Config()
    List<String> args = ["-i"]

    UBuilder(){
        this(Files.createTempDir())
    }

    UBuilder(File projectDir) {
        this.projectDir = projectDir
        File tempFile = new File(projectDir, "Assets")
        tempFile.mkdir()

        tempFile = new File(projectDir, "ProjectSettings/ProjectVersion.txt");
        tempFile.getParentFile().mkdirs()
        tempFile << "m_EditorVersion: 5.3.4f1"
        new File(projectDir, "build.gradle") << """
            plugins {
                id 'com.nxt.publish'
            }
        """

        withRepository('nxt/repo')
    }

    UBuilder saveConfig() {
        def f = new File(projectDir, "nxt/nxt.json")
        f.getParentFile().mkdirs()
        Config.save(config, f)

        f = new File(projectDir, "nxt/nxt.json.state")
        Config.save(projectState, f)

        f = new File(projectDir, PublishConfig.PUBLISH_CONFIG_PATH)
        Util.save(publishConfig, f)

        this
    }

    Project asProject() {
        saveConfig()
        Project project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        project.pluginManager.apply PublishPlugin.class
        project
    }

    GradleRunner build() {
        saveConfig()
        def runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments(args)
        runner.build()
        runner
    }

    File withFile(String path) {
        File tempFile = new File(projectDir, path)
        tempFile.getParentFile().mkdirs()
        tempFile << tempFile.name

        // Create an accompanying meta file.
        File meta = new File(tempFile.path + ".meta")
        // Put a GUID in based on file path.
        def baseURL = Paths.get(projectDir.path)
        def relativePath = baseURL.relativize(tempFile.toPath()).toFile().path
        def md5 = Hashing.md5().hashString(relativePath, Charsets.UTF_8).toString()
        meta << new Yaml().dump([guid: md5])

        tempFile
    }

    UBuilder withArg(String arg) {
        args.add(arg)
        this
    }

    UBuilder withPackage(String id) {
        withFile(IvyBuilder.assetPathForPackage(id))
        // Assume there is a top level root matching the organisation.
        String group = id.split(":")[0]
        def pack = publishConfig.addPackage(id)
        packages.add(pack)
        pack.roots.add("${group.capitalize()}/**".toString())
        saveConfig()
        this
    }

    UBuilder withRepository(String url) {
        config.addRepository(url)
        publishConfig.addRepository(url)
        saveConfig()
        this
    }

    UBuilder clearDependencies() {
        config.clearDependencies();
        saveConfig();
        this
    }

    UBuilder withDependency(String id) {
        config.addDependency(id)
        saveConfig()
        this
    }

    UBuilder removeDependency(String id) {
        config.cl
        config.addDependency(id)
        saveConfig()
        this
    }

    UBuilder withInstalledDependency(String id) {
        withFile(IvyBuilder.assetPathForPackage(id))
        projectState.addDependency(id)
        saveConfig()
        this
    }
}

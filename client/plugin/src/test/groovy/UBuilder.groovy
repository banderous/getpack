package com.nxt

import com.google.common.io.Files
import com.nxt.ProjectType
import org.gradle.testkit.runner.GradleRunner

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

    File projectDir
    Config config = new Config()
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
    }

    UBuilder create() {
        def f = new File(projectDir, "nxt/nxt.json")
        f.getParentFile().mkdirs()
        Config.save(config, f)

        f = new File(projectDir, "nxt/nxt.json.state")
        Config.save(projectState, f)
    }

    UBuilder build() {
        create()
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments(args)
            .build()
        this
    }

    File withFile(String path) {
        File tempFile = new File(projectDir, path)
        tempFile.getParentFile().mkdirs()
        tempFile << path
        tempFile
    }

    UBuilder withArg(String arg) {
        args.add(arg)
        this
    }

    UBuilder withPackage(String id) {
        withFile(filepathForPackage(id))
        config.addPackage(id)
        this
    }

    UBuilder withRepository(String url) {
        config.addRepository(url)
        this
    }

    UBuilder withDependency(String id) {
        config.addDependency(id)
        this
    }

    UBuilder withInstalledDependency(String id) {
        projectState.addDependency(id)
        this
    }

    String filepathForPackage(String id) {
        String group, name, version
        (group, name, version) = id.tokenize(':')
        "Assets/${group.capitalize()}/${name.capitalize()}/A.txt"
    }
}

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

    public static final String DUMMY_FILE = "Assets/Acme/A.txt";
    File projectDir
    Config config = new Config()
    List<String> args = ["-i"]

    UBuilder() {
        projectDir = Files.createTempDir()
        File tempFile = new File(projectDir, "Assets")
        tempFile.mkdir()

        tempFile = new File(projectDir, "ProjectSettings/ProjectVersion.txt");
        tempFile.getParentFile().mkdirs()
        tempFile << "m_EditorVersion: 5.3.4f1"
        new File(projectDir, "build.gradle") << """
            plugins {
                id 'com.nxt.publish'
            }
            group = "acme"
            version = "1.0.0"
        """
    }

    UBuilder build() {
        def f = new File(projectDir, "nxt/nxt.json")
        f.getParentFile().mkdirs()
        Config.save(config, f)

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments(args)
            .build()
        this
    }

    UBuilder withFile(String path) {
        File tempFile = new File(projectDir, path)
        tempFile.getParentFile().mkdirs()
        tempFile << path
        this
    }

    UBuilder withArg(String arg) {
        args.add(arg)
        this
    }

    UBuilder withPackage(String id) {
        config.addPackage(id)
        this
    }
}

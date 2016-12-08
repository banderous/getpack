package com.nxt;

import com.google.common.io.Files
import org.gradle.api.invocation.Gradle
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner

import java.nio.charset.StandardCharsets

public enum ProjectType {
    Empty("acme", "superjson"),
    DummyFile("acme", "superjson")

    String group, name
    public ProjectType(String group, name) {
        this.group = group
        this.name = name
    }
}

/**
 * Created by alex on 30/11/2016.
 */
class SpecHelper {

    static def dummyProject(ProjectType t) {
        ProjectBuilder.builder().withProjectDir(dummyProjectFolder(t)).build()
    }

    public static final String DUMMY_FILE = "Assets/Acme/A.txt";

    // Create a dummy project that uses our plugin.
    static def dummyProjectFolder(ProjectType projectType) {
        File tempDir = Files.createTempDir()
        File tempFile = new File(tempDir, "Assets")
        tempFile.mkdir()

        tempFile = new File(tempDir, "ProjectSettings/ProjectVersion.txt");
        tempFile.getParentFile().mkdirs()
        tempFile << "m_EditorVersion: 5.3.4f1"

        if (projectType == ProjectType.DummyFile ) {
            tempFile = new File(tempDir, DUMMY_FILE)
            tempFile.getParentFile().mkdirs()
            tempFile << "Hello"
        }

        new File(tempDir, "build.gradle") << """
            plugins {
                id 'com.nxt.publish'
            }
            group = "acme"
            version = "1.0.0"
        """

        return tempDir
    }

    static GradleRunner ProjectWithTask(ProjectType projectType, String task, String[] args = []) {
        def runner = PrepareRunner(projectType, task, args)
        if (projectType == ProjectType.DummyFile) {
            def config = new Config()
            config.addPackage("acme:superjson:1.0.0")
            def f = new File(runner.projectDir, "nxt/nxt.json")
            f.getParentFile().mkdirs()
            Config.save(config, f)
        }
        runner.build()
        runner
    }

    static GradleRunner PrepareRunner(ProjectType type, String task, String[] args) {
        def projectFolder = SpecHelper.dummyProjectFolder(type)
        println "Test for ${task} args ${args} in ${projectFolder}"

        def command = [task, "-i"]
        command.addAll(args)

        GradleRunner.create()
            .withProjectDir(projectFolder)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments(command)
    }
}

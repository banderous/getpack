package com.nxt;

import com.google.common.io.Files
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner

import java.nio.charset.StandardCharsets

public enum ProjectType {
    Empty,
    DummyFile
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
            group = "nxt"
            version = "1.0.0"
        """

        return tempDir
    }

    static def ProjectWithTask(ProjectType projectType, String task, String[] args = []) {
        def projectFolder = SpecHelper.dummyProjectFolder(projectType)
        println "Test for ${task} args ${args} in ${projectFolder}"
        def command = [task, "-i"]
        command.addAll(args)
        GradleRunner r = GradleRunner.create()
                .withProjectDir(projectFolder)
                .withArguments(command)
                .withPluginClasspath()
                .forwardOutput()
        r.build()
        return projectFolder
    }
}

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

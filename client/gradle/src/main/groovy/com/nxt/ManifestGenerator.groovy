package com.nxt

import groovy.json.JsonBuilder
import org.gradle.api.Project

import java.nio.file.Paths
import java.security.MessageDigest

/**
 * Created by alex on 30/11/2016.
 */
class ManifestGenerator {
    public static String GenerateManifest(Project project) {
        def builder = new JsonBuilder()
        def tree = project.fileTree('Assets') {
            exclude "**/*.meta"
        }

        // Relativize the paths to the project root,
        // so they start 'Assets/...".
        def baseURL = Paths.get(project.file('.').absolutePath)
        builder files : tree.collectEntries {
            [("${baseURL.relativize(it.toPath()).toFile().path}"): [md5: generateMD5(it)]]
        }

        builder.toString()
    }

    static String generateMD5(File f) {
        def digest = MessageDigest.getInstance("MD5")
        f.eachByte(4096) { buffer, length ->
            digest.update(buffer, 0, length)
        }
        return digest.digest().encodeHex() as String
    }
}
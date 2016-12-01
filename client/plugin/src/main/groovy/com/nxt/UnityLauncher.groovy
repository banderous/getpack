package com.nxt


/**
 * Created by alex on 30/11/2016.
 */
class UnityLauncher {

    public static String UnityVersion(String projectPath) {
        def versionFile = new File(projectPath, "ProjectSettings/ProjectVersion.txt")

        if (!versionFile.exists()) {
            throw new IllegalArgumentException("Project not found at ${projectPath}")
        }

        def  version = versionFile.readLines().get(0)
        return version.split(":")[1].trim()
    }
}

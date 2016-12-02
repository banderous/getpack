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

    public static File UnityPathForVersion(File searchPath, String version) {
        def result = searchPath.listFiles().find { file ->
            if (file.isDirectory()) {
                def pList = new File(file, "Unity.app/Contents/Info.plist")
                if (pList.exists()) {
                    def parser = new XmlSlurper()
                    parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
                    def next = false
                    // Search for bundle version.
                    def node = parser.parse(pList).dict.'*'.find { node ->
                        if (next) {
                            return true
                        }
                        next = node.name() == "key" && node.text() == "CFBundleVersion"
                        false
                    }

                    if (node.text() == version) {
                        return file
                    }
                }
            }
        }

        if (!result) {
            throw new IllegalArgumentException("Unity ${version} not found")
        }
        result
    }
}
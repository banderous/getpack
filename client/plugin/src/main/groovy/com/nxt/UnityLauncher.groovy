package com.nxt

import java.nio.channels.OverlappingFileLockException


/**
 * Created by alex on 30/11/2016.
 */
class UnityLauncher {

    public static String UnityVersion(File projectPath) {
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
                    println "reading " + pList.path
                    def parser = new XmlSlurper(false, false, true)
                    parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
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

    public static boolean IsUnityRunning(File projectPath) {
        def lockFile = new File(projectPath, "Temp/UnityLockfile")
        if (!lockFile.exists()) {
            return false;
        }
        try {
            def lock = new FileOutputStream(lockFile).getChannel().lock()
            if (lock) {
                lock.release()
                return false
            }
        } catch (OverlappingFileLockException e) {
            println e
        }
        return true;
    }

    public static File UnityExeForVersion(File searchPath, String version) {
        // TODO: Windows
        def unityPath = UnityPathForVersion(searchPath, version)
        return new File(unityPath, 'Unity.app/Contents/MacOS/Unity')
    }
}

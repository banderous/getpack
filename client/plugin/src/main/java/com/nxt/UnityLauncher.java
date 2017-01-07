package com.nxt;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.nxt.config.Util;
import org.gradle.api.GradleException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Map;


/**
 * Created by alex on 30/11/2016.
 */
public class UnityLauncher {

    public static String UnityVersion(File projectPath) {
        File versionFile = new File(projectPath, "ProjectSettings/ProjectVersion.txt");

        if (!versionFile.exists()) {
            Log.L.debug("No version file found for " + projectPath);
            return null;
        }

        try {
            String  version = Files.readLines(versionFile, Charsets.UTF_8).get(0);
            return version.split(":")[1].trim();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Map<String, File> FindInstalledEditorsOSX(File searchPath) {
        Map<String, File> result = Maps.newHashMap();
        for (File file : searchPath.listFiles()) {
            if (file.isDirectory()) {
                File pList = new File(file, "Unity.app/Contents/Info.plist");
                if (pList.exists()) {
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String expression = "/plist/dict/key[text() = 'CFBundleVersion']/following-sibling::string/text()";
                    try {
                        Document doc = NonValidatingDoc(new InputSource(new FileInputStream(pList)));
                        String installedVersion = xpath.evaluate(expression, doc);
                        File executable = new File(file, "Unity.app/Contents/MacOS/Unity");
                        result.put(installedVersion, executable);
                    } catch (XPathExpressionException e) {
                        throw new RuntimeException(e);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return result;
    }

    public static File SelectEditorForProject(File project) {
        String version = UnityVersion(project);
        Map<String, File> installedEditors;
        if (Util.OnOSX()) {
            installedEditors = FindInstalledEditorsOSX(new File("/Applications"));
        } else if (Util.OnWindows()) {
            installedEditors = FindInstalledEditorsWindows(new File("c:/Program Files"),
                    new File("C:/Program Files (x86)"));
        } else {
            throw new GradleException("Only Windows and Mac Supported");
        }
        return SelectEditor(installedEditors, version);
    }

    public static File SelectEditor(Map<String, File> editors, String projectVersion) {
        if (null == projectVersion) {
            String highestVersion = ImmutableSortedSet.copyOf(editors.keySet()).last();
            Log.L.debug("Selecting highest editor version: {}", highestVersion);
            return editors.get(highestVersion);
        }

        if (editors.containsKey(projectVersion)) {
            return editors.get(projectVersion);
        }

        throw new IllegalArgumentException("Required editor version not installed: " + projectVersion);
    }

    private static Document NonValidatingDoc(InputSource source) {
        DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();
        dbfact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
        try {
            DocumentBuilder builder = dbfact.newDocumentBuilder();
            return builder.parse(source);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean IsUnityRunning(File projectPath) {
        File lockFile = new File(projectPath, "Temp/UnityLockfile");
        if (!lockFile.exists()) {
            return false;
        }
        try(FileOutputStream out = new FileOutputStream(lockFile)) {
            FileLock lock = out.getChannel().tryLock();
            // If unable to lock then Unity is running.
            return lock == null;
        } catch (OverlappingFileLockException e) {
            // A lock is already held.
            return true;
        } catch (FileNotFoundException f) {
            Log.L.info("FileNotFoundException opening lock file, Unity running.");
            // Java throws a FileNotFoundException if the file is locked.
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, File> FindInstalledEditorsWindows(File... searchPaths) {
        Map<String, File> result = Maps.newHashMap();
        for (File searchPath : searchPaths) {
            for (File file : searchPath.listFiles()) {
                if (file.isDirectory()) {
                    File packageManager = new File(file, "Editor/Data/PackageManager/Unity//PackageManager");
                    if (packageManager.exists()) {
                        String[] contents = packageManager.list();
                        if (null != contents && contents.length == 1) {
                            File ivy = new File(packageManager, contents[0] + "/ivy.xml");
                            if (ivy.exists()) {
                                XPath xpath = XPathFactory.newInstance().newXPath();
                                String expression = "/ivy-module/info/@unityVersion";
                                try {
                                    Document doc = NonValidatingDoc(new InputSource(new FileInputStream(ivy)));
                                    String installedVersion = xpath.evaluate(expression, doc);
                                    File executable = new File(file, "Editor/Unity.exe");
                                    result.put(installedVersion, executable);
                                } catch (XPathExpressionException e) {
                                    throw new RuntimeException(e);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;

    }
}

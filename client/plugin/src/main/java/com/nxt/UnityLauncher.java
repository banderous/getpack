package com.nxt;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

    public static Map<String, File> FindInstalledEditors(File searchPath) {
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
        return SelectEditor(FindInstalledEditors(new File("/Applications")), version);
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
        FileLock lock = null;
        try {
            lock = new FileOutputStream(lockFile).getChannel().tryLock();
            // If unable to lock then Unity is running.
            return lock == null;
        } catch (OverlappingFileLockException e) {
            // A lock is already held.
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            if (null != lock) {
                try {
                    lock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

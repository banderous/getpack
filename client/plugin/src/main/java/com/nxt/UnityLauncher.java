package com.nxt;

import com.google.common.base.Charsets;
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


/**
 * Created by alex on 30/11/2016.
 */
public class UnityLauncher {

    public static String UnityVersion(File projectPath) {
        File versionFile = new File(projectPath, "ProjectSettings/ProjectVersion.txt");

        if (!versionFile.exists()) {
            throw new IllegalArgumentException("Project not found at ${projectPath}");
        }

        try {
            String  version = Files.readLines(versionFile, Charsets.UTF_8).get(0);
            return version.split(":")[1].trim();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static File UnityPathForVersion(File searchPath, String version) {
        for (File file : searchPath.listFiles()) {
            if (file.isDirectory()) {
                File pList = new File(file, "Unity.app/Contents/Info.plist");
                if (pList.exists()) {
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String expression = "/plist/dict/key[text() = 'CFBundleVersion']/following-sibling::string/text()";
                    try {
                        Document doc = NonValidatingDoc(new InputSource(new FileInputStream(pList)));
                        String installedVersion = xpath.evaluate(expression, doc);
                        if (installedVersion.equals(version)) {
                            return file;
                        }
                    } catch (XPathExpressionException e) {
                        throw new RuntimeException(e);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        throw new IllegalArgumentException("Unity version not found: " + version);
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

    public static File UnityExeForVersion(File searchPath, String version) {
        // TODO: Windows
        File unityPath = UnityPathForVersion(searchPath, version);
        return new File(unityPath, "Unity.app/Contents/MacOS/Unity");
    }
}

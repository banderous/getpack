package com.nxt;
import com.google.common.io.ByteStreams;

import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.gzip.*;
import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Created by alex on 15/12/2016.
 */
public class CreateTarGZ {
    public static void Create(File folder, File destination)
    {
        TarArchiveOutputStream tOut = null;
        try {
            FileOutputStream fOut = new FileOutputStream(destination);
            BufferedOutputStream bOut = new BufferedOutputStream(fOut);
            GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(bOut);

            tOut = new TarArchiveOutputStream(gzOut);
            addFileToTarGz(tOut, folder, ".");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (tOut != null) {
                try {
                    tOut.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void addFileToTarGz(TarArchiveOutputStream tOut, File f, String entryName)
            throws IOException
    {
        TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
        tOut.putArchiveEntry(tarEntry);

        if (f.isFile()) {
            ByteStreams.copy(new FileInputStream(f), tOut);
            tOut.closeArchiveEntry();
        } else {
            tOut.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToTarGz(tOut, child, entryName + "/" + child.getName());
                }
            }
        }
    }
}

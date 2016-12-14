package com.nxt.config;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;

class Asset {
    String md5;
}

class AssetMap extends HashMap<String, Asset> {}

/**
 * Created by alex on 09/12/2016.
 */
public class PackageManifest {

    // Pathname
    AssetMap files = new AssetMap();

    public static PackageManifest load(File from) {
        try {
            return new Gson().fromJson(new FileReader(from), PackageManifest.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save(PackageManifest manifest, File to) {
        try {
            Files.write(new Gson().toJson(manifest), to, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

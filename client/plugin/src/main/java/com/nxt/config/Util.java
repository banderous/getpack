package com.nxt.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by alex on 01/01/2017.
 */
public class Util {
    public static <T> T LoadJSONClass(File f, Class<T> c) {
        try {
            if (!f.exists()) {
                Files.createParentDirs(f);
                Files.write("{}", f, Charsets.UTF_8);
            }
            return new Gson().fromJson(new FileReader(f), c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void save(T object, File f) {
        try {
            Files.write(new Gson().toJson(object), f, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.nxt.config;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class Asset {
    private String md5, path;
    private Asset() {}
    public transient PackageManifest pack;

    Asset(String path, String md5) {
        this.path = path;
        this.md5 = md5;
    }

    public String getMd5() {
        return md5;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        final Asset other = (Asset) obj;
        if (null == other) return false;

        return Objects.equals(md5, other.md5)
                && Objects.equals(path, other.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, md5);
    }

    @Override
    public String toString() {
        return Joiner.on(":").join(path, md5);
    }
}

package com.nxt.config;

import java.nio.file.Path;
import java.util.Objects;

public class Asset {
    private String md5, path;
    private Asset() {}
    public transient PackageManifest pack;

    Asset(Path path, String md5) {
        this.path = path.toString();
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
}

package com.nxt.config;

import java.nio.file.Path;
import java.util.Objects;

public class Asset {
    String md5, path;
    private Asset() {}
    Asset(Path path, String md5) {
        this.path = path.toString();
        this.md5 = md5;
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

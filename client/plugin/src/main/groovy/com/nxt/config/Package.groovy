package com.nxt

/**
 * Created by alex on 08/12/2016.
 */
class Package {
    String group, name, version
    List<String> roots = []

    // Required for serialization.
    Package() {
    }

    Package(String id) {
        (group, name, version) = id.tokenize(':')
    }


    String key() {
        "${group}:${name}"
    }
}

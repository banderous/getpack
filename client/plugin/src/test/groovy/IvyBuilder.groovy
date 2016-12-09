package com.nxt

import com.google.common.io.Files

/**
 * Created by alex on 09/12/2016.
 */
class IvyBuilder {
    static IvyBuilder Create() {
        new IvyBuilder()
    }

    File dir = Files.createTempDir()

    IvyBuilder withPackage(String id) {
        String group, name, version
        (group, name, version) = id.tokenize(":")
        def ivyFolder = new File(dir, "${group}/${name}/${version}")
        ivyFolder.mkdirs()
        def ivy = new File(ivyFolder, "ivy-${version}.xml")

        ivy << """<?xml version="1.0" encoding="UTF-8"?>
<ivy-module version="2.0">
    <info organisation="${group}" module="${name}" revision="${version}" status="integration" publication="20161209071257"/>
    <configurations/>
    <publications>
        <artifact name="${name}" type="unitypackage" ext="unitypackage"/>
</publications>
<dependencies/>
</ivy-module>
        """

        File dummyPackage = new File("src/test/resources/unitypackage/dummy.unitypackage")
        File unityPackage = new File(ivyFolder, "${name}-${version}.unitypackage")
        Files.copy(dummyPackage, unityPackage)
        this
    }
}

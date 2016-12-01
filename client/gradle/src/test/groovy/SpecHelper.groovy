import com.google.common.io.Files
import org.gradle.testfixtures.ProjectBuilder

import java.nio.charset.StandardCharsets

/**
 * Created by alex on 30/11/2016.
 */
class SpecHelper {

    static def dummyProject() {
        ProjectBuilder.builder().withProjectDir(dummyProjectFolder()).build()
    }

    // Create a dummy project that uses our plugin.
    static def dummyProjectFolder() {
        def tempDir = Files.createTempDir()
        def tempFile = new File(tempDir, "Assets/Acme/A.txt")
        tempFile.getParentFile().mkdirs()
        Files.write("Hello", tempFile, StandardCharsets.UTF_8)

        new File(tempDir, "build.gradle") << """
            plugins {
                id 'com.nxt.publish'
            }
        """

        return tempDir
    }
}

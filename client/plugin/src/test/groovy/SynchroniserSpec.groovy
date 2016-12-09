package com.nxt
import com.google.common.io.Files
import com.nxt.UBuilder
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Created by alex on 09/12/2016.
 */
class SynchroniserSpec extends Specification {
    def deps = []
    def superJSON = "acme:superjson:1.0.0"

    def project = ProjectBuilder.builder().withProjectDir(Files.createTempDir()).build()
    def projConf = UBuilder.Builder(project.projectDir)

    def ivyRepo = IvyBuilder.Create().withPackage(superJSON)

    def setup (){
        projConf.withRepository(ivyRepo.dir.path)
    }

    def "detects new package"() {
        when:
        projConf.withDependency(superJSON)
        def deps = Synchroniser.resolveDeps(project, projConf.config, projConf.projectState)

        then:
        deps.added.containsKey 'acme:superjson'
    }

    def "detects removed package"() {
        when:
        projConf.withInstalledDependency(superJSON)

        def deps = Synchroniser.resolveDeps(project, projConf.config, projConf.projectState)

        then:
        deps.removed.containsKey 'acme:superjson'
    }

    def "detects changed package"() {
        when:
        def newVer = "acme:superjson:1.0.1"
        ivyRepo.withPackage(newVer)
        // A new version is requested.
        projConf.withDependency(newVer)
        // Old one still installed.
        projConf.withInstalledDependency(superJSON)

        def deps = Synchroniser.resolveDeps(project, projConf.config, projConf.projectState)

        then:
        deps.changed.containsKey 'acme:superjson'
    }

    def "first execution"() {
        when:
        def builder = IvyBuilder.Create()
                .withPackage("acme:superjson:1.0.0")

        then:
        def file = new File(builder.dir, "acme/superjson/1.0.0/ivy-1.0.0.xml")
        println file
    }
}

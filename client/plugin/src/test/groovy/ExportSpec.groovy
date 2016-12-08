package com.nxt

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder;
import spock.lang.Specification
import org.gradle.api.GradleException

class ExportSpec extends Specification {

    def project = ProjectBuilder.builder().build()
    def id = "acme:superjson:1.0.1"

    def "creates export task"() {

        when:
        def config = new Config()
        config.addPackage(id)
        ExportPackage.Configure(project, config)

        then:
        project.tasks.nxtExportAcmeSuperjson



    }
}

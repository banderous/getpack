package com.nxt

import groovy.json.JsonSlurper
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import com.nxt.Trouble
import static SpecHelper.ProjectWithTask;

class ExportPackageSpec extends BaseE2ESpec {

    @Trouble
    def "Describes the export job"() {
        when:
        def projectFolder = ProjectWithTask(ProjectType.DummyFile, "nxtCreateExportJob")
        def task = new File(projectFolder, ExportPackage.TASK_PATH)
        def exportJob = new JsonSlurper().parse(task)

        then:
        exportJob.task.files instanceof List
        exportJob.task.files.size == 1
        exportJob.task.files[0] == "Assets/Acme/A.txt"
    }
}

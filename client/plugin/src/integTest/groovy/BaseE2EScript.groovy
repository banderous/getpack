package com.nxt;

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import com.nxt.Trouble
import static SpecHelper.ProjectWithTask

class BaseE2ESpec extends Specification {
    def cleanupSpec() {
        // Kill all the Unity processes we start.
        "pkill Unity".execute()
    }
}

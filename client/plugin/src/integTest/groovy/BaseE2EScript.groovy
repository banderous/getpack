package com.nxt

import com.nxt.config.Util;
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import com.nxt.Trouble

class BaseE2ESpec extends Specification {
    def cleanupSpec() {
        // Kill all the Unity processes we start.
        if (Util.OnOSX()) {
            "pkill Unity".execute()
        }
    }
}

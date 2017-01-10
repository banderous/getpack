package com.nxt

import com.nxt.config.Util
import spock.lang.Specification

class BaseE2ESpec extends Specification {
    def cleanupSpec() {
        // Kill all the Unity processes we start.
        if (Util.onOSX()) {
            "pkill Unity".execute()
        } else if (Util.onWindows()) {
            "taskkill /F /IM Unity.exe /T".execute()
        }
    }
}

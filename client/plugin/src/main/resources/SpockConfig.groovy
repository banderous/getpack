import com.nxt.Trouble;

runner {
    if (System.properties['test.trouble']) {
        // Only run tests annoted with @Trouble.
        include Trouble
    }
}

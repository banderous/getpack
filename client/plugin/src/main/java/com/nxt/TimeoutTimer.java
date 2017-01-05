package com.nxt;

import org.gradle.api.GradleException;

/**
 * Created by alex on 05/01/2017.
 */
public class TimeoutTimer {
    private long startTimeMillis;
    private long durationMillis;
    private String message;
    public TimeoutTimer(int timeoutInSeconds, String message) {
        this.durationMillis = timeoutInSeconds * 1000;
        this.startTimeMillis = System.currentTimeMillis();
        this.message = message;
    }


    public void throwIfExceeded() {
        if (System.currentTimeMillis() - startTimeMillis > durationMillis) {
            throw new GradleException(message);
        }
    }
}

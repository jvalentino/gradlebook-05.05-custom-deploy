package com.blogspot.jvalentino.gradle.service

/**
 * <p>An exception</p>
 * @author jvalentino2
 */
class TimeoutException extends Exception {

    String message

    TimeoutException(String message) {
        this.message = message
    }
}

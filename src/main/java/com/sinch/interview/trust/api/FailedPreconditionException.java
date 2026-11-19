package com.sinch.interview.trust.api;

public class FailedPreconditionException extends RuntimeException {

    public FailedPreconditionException(String message) {
        super(message);
    }
}

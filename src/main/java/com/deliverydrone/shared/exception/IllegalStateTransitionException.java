package com.deliverydrone.shared.exception;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String entityName, String from, String to) {
        super("Cannot change " + entityName + " from " + from + " to " + to + ".");
    }
}

package com.deliverydrone.shared.exception;

public class NoFreeLandingPadException extends RuntimeException {
    public NoFreeLandingPadException(String zoneName) {
        super("There is no free landing pad in zone " + zoneName + ".");
    }
}

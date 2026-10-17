package com.deliverydrone.shared.exception;

public class PayloadTooHeavyException extends RuntimeException {
    public PayloadTooHeavyException(double payloadKg, double maxPayloadKg) {
        super("Payload " + payloadKg + " kg is above the drone limit of " + maxPayloadKg + " kg.");
    }
}

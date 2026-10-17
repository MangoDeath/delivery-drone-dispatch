package com.deliverydrone.landing;

import com.deliverydrone.shared.exception.IllegalStateTransitionException;

public class DeliveryConfirmation {
    private final String orderId;
    private final String padId;
    private final String code;
    private DeliveryConfirmationStatus status;

    public DeliveryConfirmation(String orderId, String padId, String code,
                                DeliveryConfirmationStatus status) {
        this.orderId = orderId;
        this.padId = padId;
        this.code = code;
        this.status = status;
    }

    public void confirm() {
        if (status != DeliveryConfirmationStatus.PENDING) {
            throw new IllegalStateTransitionException("DeliveryConfirmation", status.name(),
                    DeliveryConfirmationStatus.CONFIRMED.name());
        }
        status = DeliveryConfirmationStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == DeliveryConfirmationStatus.CANCELLED) {
            throw new IllegalStateTransitionException("DeliveryConfirmation", status.name(),
                    DeliveryConfirmationStatus.CANCELLED.name());
        }
        status = DeliveryConfirmationStatus.CANCELLED;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPadId() {
        return padId;
    }

    public String getCode() {
        return code;
    }

    public DeliveryConfirmationStatus getStatus() {
        return status;
    }

    public String toString() {
        return orderId + ": " + code + " is " + status;
    }
}

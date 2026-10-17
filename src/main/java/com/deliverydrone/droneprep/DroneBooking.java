package com.deliverydrone.droneprep;

import com.deliverydrone.shared.exception.IllegalStateTransitionException;

public class DroneBooking {
    private final String orderId;
    private final String droneId;
    private final double payloadKg;
    private DroneBookingStatus status;

    public DroneBooking(String orderId, String droneId, double payloadKg,
                        DroneBookingStatus status) {
        this.orderId = orderId;
        this.droneId = droneId;
        this.payloadKg = payloadKg;
        this.status = status;
    }

    public void load() {
        if (status != DroneBookingStatus.RESERVED) {
            throw new IllegalStateTransitionException("DroneBooking", status.name(),
                    DroneBookingStatus.LOADED.name());
        }
        status = DroneBookingStatus.LOADED;
    }

    public void release() {
        if (status == DroneBookingStatus.RELEASED) {
            throw new IllegalStateTransitionException("DroneBooking", status.name(),
                    DroneBookingStatus.RELEASED.name());
        }
        // Releasing is allowed from RESERVED or LOADED to make compensation possible.
        status = DroneBookingStatus.RELEASED;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getDroneId() {
        return droneId;
    }

    public double getPayloadKg() {
        return payloadKg;
    }

    public DroneBookingStatus getStatus() {
        return status;
    }

    public String toString() {
        return orderId + " uses " + droneId + " and is " + status;
    }
}

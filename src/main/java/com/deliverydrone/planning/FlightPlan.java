package com.deliverydrone.planning;

import com.deliverydrone.shared.exception.IllegalStateTransitionException;

public class FlightPlan {
    private final String orderId;
    private final String zoneName;
    private final double distanceKm;
    private final double travelTimeMinutes;
    private FlightPlanStatus status;

    public FlightPlan(String orderId, String zoneName, double distanceKm,
                      double travelTimeMinutes, FlightPlanStatus status) {
        this.orderId = orderId;
        this.zoneName = zoneName;
        this.distanceKm = distanceKm;
        this.travelTimeMinutes = travelTimeMinutes;
        this.status = status;
    }

    public void confirm() {
        if (status != FlightPlanStatus.HELD) {
            throw new IllegalStateTransitionException("FlightPlan", status.name(),
                    FlightPlanStatus.CONFIRMED.name());
        }
        status = FlightPlanStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == FlightPlanStatus.CANCELLED) {
            throw new IllegalStateTransitionException("FlightPlan", status.name(),
                    FlightPlanStatus.CANCELLED.name());
        }
        // A later dispatch step can fail, so a confirmed plan may be cancelled too.
        status = FlightPlanStatus.CANCELLED;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getTravelTimeMinutes() {
        return travelTimeMinutes;
    }

    public FlightPlanStatus getStatus() {
        return status;
    }

    public String toString() {
        return orderId + " to " + zoneName + " is " + status;
    }
}

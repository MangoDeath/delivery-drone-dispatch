package com.deliverydrone.strategy.model;

public class DeliveryRequest {
    private final String zoneName;
    private final double distanceKm;
    private final double payloadKg;
    private final boolean urgent;

    public DeliveryRequest(String zoneName, double distanceKm, double payloadKg, boolean urgent) {
        this.zoneName = zoneName;
        this.distanceKm = distanceKm;
        this.payloadKg = payloadKg;
        this.urgent = urgent;
    }

    public String getZoneName() {
        return zoneName;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getPayloadKg() {
        return payloadKg;
    }

    public boolean isUrgent() {
        return urgent;
    }
}

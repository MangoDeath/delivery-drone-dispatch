package com.deliverydrone.strategy.model;

public class DroneOption {
    private final String id;
    private final double pickupDistanceKm;
    private final double batteryPercent;
    private final double maxPayloadKg;
    private final int activeDeliveries;

    public DroneOption(String id, double pickupDistanceKm, double batteryPercent,
                       double maxPayloadKg, int activeDeliveries) {
        this.id = id;
        this.pickupDistanceKm = pickupDistanceKm;
        this.batteryPercent = batteryPercent;
        this.maxPayloadKg = maxPayloadKg;
        this.activeDeliveries = activeDeliveries;
    }

    public String getId() {
        return id;
    }

    public double getPickupDistanceKm() {
        return pickupDistanceKm;
    }

    public double getBatteryPercent() {
        return batteryPercent;
    }

    public double getMaxPayloadKg() {
        return maxPayloadKg;
    }

    public int getActiveDeliveries() {
        return activeDeliveries;
    }
}

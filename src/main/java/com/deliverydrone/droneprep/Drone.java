package com.deliverydrone.droneprep;

public class Drone {
    private final String id;
    private final double batteryPercent;
    private final double maxPayloadKg;

    public Drone(String id, double batteryPercent, double maxPayloadKg) {
        this.id = id;
        this.batteryPercent = batteryPercent;
        this.maxPayloadKg = maxPayloadKg;
    }

    public String getId() {
        return id;
    }

    public double getBatteryPercent() {
        return batteryPercent;
    }

    public double getMaxPayloadKg() {
        return maxPayloadKg;
    }

    public String toString() {
        return id + ": battery=" + batteryPercent + "%, max=" + maxPayloadKg + " kg";
    }
}

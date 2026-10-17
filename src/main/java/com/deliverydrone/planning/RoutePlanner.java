package com.deliverydrone.planning;

public class RoutePlanner {
    private static final double DRONE_SPEED_KM_PER_HOUR = 40.0;

    public double calculateDistanceKm(Zone zone) {
        double xPart = zone.getX() * zone.getX();
        double yPart = zone.getY() * zone.getY();
        return Math.sqrt(xPart + yPart);
    }

    public double calculateTravelTimeMinutes(double distanceKm) {
        double hours = distanceKm / DRONE_SPEED_KM_PER_HOUR;
        return hours * 60.0;
    }
}

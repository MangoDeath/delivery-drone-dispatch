package com.deliverydrone.planning;

public class Zone {
    private final String name;
    private final double x;
    private final double y;
    private final double windSpeed;

    public Zone(String name, double x, double y, double windSpeed) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.windSpeed = windSpeed;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String toString() {
        return name + " (" + x + ", " + y + "), wind=" + windSpeed;
    }
}

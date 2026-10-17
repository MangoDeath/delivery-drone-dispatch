package com.deliverydrone.shared.exception;

public class LowBatteryException extends RuntimeException {
    public LowBatteryException(double batteryPercent, double neededPercent) {
        super("Battery is " + batteryPercent + "% but route needs " + neededPercent + "%.");
    }
}

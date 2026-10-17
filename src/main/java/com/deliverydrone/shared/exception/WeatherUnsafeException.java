package com.deliverydrone.shared.exception;

public class WeatherUnsafeException extends RuntimeException {
    public WeatherUnsafeException(double windSpeed) {
        super("Wind speed " + windSpeed + " m/s is above the 10 m/s limit.");
    }
}

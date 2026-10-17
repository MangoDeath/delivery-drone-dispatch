package com.deliverydrone.planning;

import com.deliverydrone.shared.exception.WeatherUnsafeException;

public class WeatherChecker {
    private static final double MAX_WIND_SPEED = 10.0;

    public void check(Zone zone) {
        // This check is done before saving a flight plan, so unsafe weather saves nothing.
        if (zone.getWindSpeed() > MAX_WIND_SPEED) {
            throw new WeatherUnsafeException(zone.getWindSpeed());
        }
    }
}

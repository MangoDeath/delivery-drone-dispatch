package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.weather.WindSource;

public final class FlightTimeStrategies {
    private FlightTimeStrategies() {
    }

    public static FlightTimeStrategy standard() {
        return new SwitchFlightTimeStrategy(FlightTimeMode.STANDARD, null);
    }

    public static FlightTimeStrategy express() {
        return new SwitchFlightTimeStrategy(FlightTimeMode.EXPRESS, null);
    }

    public static FlightTimeStrategy weather(WindSource windSource) {
        return new SwitchFlightTimeStrategy(FlightTimeMode.WEATHER, windSource);
    }
}

package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.weather.WindSource;

public final class FlightTimeStrategies {
    private FlightTimeStrategies() {
    }

    public static FlightTimeStrategy standard() {
        return new StandardFlightTimeStrategy();
    }

    public static FlightTimeStrategy express() {
        return new ExpressFlightTimeStrategy();
    }

    public static FlightTimeStrategy weather(WindSource windSource) {
        return new WeatherFlightTimeStrategy(windSource);
    }
}

package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.weather.WindSource;

public class FlightTimeSelector {
    private static final double STRONG_WIND = 7.0;
    private final WindSource windSource;

    public FlightTimeSelector(WindSource windSource) {
        this.windSource = windSource;
    }

    public FlightTimeStrategy selectFor(DeliveryRequest request) {
        if (request.isUrgent()) {
            return new ExpressFlightTimeStrategy();
        }
        if (windSource.windSpeedFor(request.getZoneName()) > STRONG_WIND) {
            return new WeatherFlightTimeStrategy(windSource);
        }
        return new StandardFlightTimeStrategy();
    }
}

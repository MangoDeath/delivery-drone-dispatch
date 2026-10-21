package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.weather.WindSource;

public class WeatherFlightTimeStrategy implements FlightTimeStrategy {
    private final WindSource windSource;

    public WeatherFlightTimeStrategy(WindSource windSource) {
        this.windSource = windSource;
    }

    @Override
    public double estimateMinutes(DeliveryRequest request) {
        double speed = Math.max(10.0, 40.0 - windSource.windSpeedFor(request.getZoneName()));
        return request.getDistanceKm() / speed * 60.0;
    }
}

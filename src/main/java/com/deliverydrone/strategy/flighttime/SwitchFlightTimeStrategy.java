package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.weather.WindSource;

class SwitchFlightTimeStrategy implements FlightTimeStrategy {
    private final FlightTimeMode mode;
    private final WindSource windSource;

    SwitchFlightTimeStrategy(FlightTimeMode mode, WindSource windSource) {
        this.mode = mode;
        this.windSource = windSource;
    }

    @Override
    public double estimateMinutes(DeliveryRequest request) {
        switch (mode) {
            case STANDARD:
                return request.getDistanceKm() / 40.0 * 60.0;
            case EXPRESS:
                return 5.0 + request.getDistanceKm() / 60.0 * 60.0;
            case WEATHER:
                double speed = Math.max(10.0, 40.0 - windSource.windSpeedFor(request.getZoneName()));
                return request.getDistanceKm() / speed * 60.0;
            default:
                throw new IllegalStateException("Unknown flight-time mode.");
        }
    }
}

package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.model.DeliveryRequest;

public class StandardFlightTimeStrategy implements FlightTimeStrategy {
    @Override
    public double estimateMinutes(DeliveryRequest request) {
        return request.getDistanceKm() / 40.0 * 60.0;
    }
}

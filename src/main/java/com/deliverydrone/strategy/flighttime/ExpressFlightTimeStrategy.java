package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.model.DeliveryRequest;

public class ExpressFlightTimeStrategy implements FlightTimeStrategy {
    @Override
    public double estimateMinutes(DeliveryRequest request) {
        return 5.0 + request.getDistanceKm() / 60.0 * 60.0;
    }
}

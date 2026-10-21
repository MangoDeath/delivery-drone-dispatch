package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.model.DeliveryRequest;

public interface FlightTimeStrategy {
    double estimateMinutes(DeliveryRequest request);
}

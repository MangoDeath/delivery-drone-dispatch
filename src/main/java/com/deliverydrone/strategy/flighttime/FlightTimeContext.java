package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.model.DeliveryRequest;

import java.util.Objects;

public class FlightTimeContext {
    private FlightTimeStrategy strategy;

    public FlightTimeContext(FlightTimeStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(FlightTimeStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy);
    }

    public double estimateMinutes(DeliveryRequest request) {
        return strategy.estimateMinutes(request);
    }
}

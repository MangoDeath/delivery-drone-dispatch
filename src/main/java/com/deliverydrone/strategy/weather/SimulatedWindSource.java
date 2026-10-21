package com.deliverydrone.strategy.weather;

import java.util.Map;

public class SimulatedWindSource implements WindSource {
    private final Map<String, Double> windByZone = Map.of(
            "North", 3.0,
            "East", 6.0,
            "Windy", 12.0,
            "Storm", 18.0
    );

    @Override
    public double windSpeedFor(String zoneName) {
        return windByZone.getOrDefault(zoneName, 4.0);
    }
}

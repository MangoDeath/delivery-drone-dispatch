package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.weather.WindSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlightTimeBehaviorTest {
    private final DeliveryRequest request = new DeliveryRequest("North", 20, 3, false);
    private final WindSource windSource = zoneName -> 10;

    @Test
    void calculatesStandardTime() {
        assertEquals(30, FlightTimeStrategies.standard().estimateMinutes(request), 0.001);
    }

    @Test
    void calculatesExpressTime() {
        assertEquals(25, FlightTimeStrategies.express().estimateMinutes(request), 0.001);
    }

    @Test
    void calculatesWeatherAdjustedTime() {
        assertEquals(40, FlightTimeStrategies.weather(windSource).estimateMinutes(request), 0.001);
    }
}

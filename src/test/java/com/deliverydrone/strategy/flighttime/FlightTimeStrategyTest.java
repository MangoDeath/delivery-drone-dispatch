package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.weather.WindSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightTimeStrategyTest {
    private final DeliveryRequest request = new DeliveryRequest("North", 20, 3, false);

    @Test
    void standardStrategyUsesNormalSpeed() {
        assertEquals(30, new StandardFlightTimeStrategy().estimateMinutes(request), 0.001);
    }

    @Test
    void expressStrategyIncludesLaunchPreparation() {
        assertEquals(25, new ExpressFlightTimeStrategy().estimateMinutes(request), 0.001);
    }

    @Test
    void weatherStrategyUsesFixedWindSource() {
        WindSource windSource = new FixedWindSource(10);
        assertEquals(40, new WeatherFlightTimeStrategy(windSource).estimateMinutes(request), 0.001);
    }

    @ParameterizedTest
    @MethodSource("allStrategies")
    void timeIsNeverNegative(FlightTimeStrategy strategy) {
        assertTrue(strategy.estimateMinutes(request) >= 0);
    }

    private static Stream<FlightTimeStrategy> allStrategies() {
        return Stream.of(
                new StandardFlightTimeStrategy(),
                new ExpressFlightTimeStrategy(),
                new WeatherFlightTimeStrategy(new FixedWindSource(10))
        );
    }

    private static class FixedWindSource implements WindSource {
        private final double windSpeed;

        FixedWindSource(double windSpeed) {
            this.windSpeed = windSpeed;
        }

        @Override
        public double windSpeedFor(String zoneName) {
            return windSpeed;
        }
    }
}

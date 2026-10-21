package com.deliverydrone.strategy.flighttime;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.weather.WindSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FlightTimeSelectorTest {
    private final WindSource windSource = zoneName -> zoneName.equals("Windy") ? 12 : 3;
    private final FlightTimeSelector selector = new FlightTimeSelector(windSource);

    @Test
    void choosesExpressForUrgentRequest() {
        assertInstanceOf(ExpressFlightTimeStrategy.class,
                selector.selectFor(new DeliveryRequest("North", 20, 3, true)));
    }

    @Test
    void choosesWeatherStrategyForStrongWind() {
        assertInstanceOf(WeatherFlightTimeStrategy.class,
                selector.selectFor(new DeliveryRequest("Windy", 20, 3, false)));
    }

    @Test
    void choosesStandardStrategyForCalmWeather() {
        assertInstanceOf(StandardFlightTimeStrategy.class,
                selector.selectFor(new DeliveryRequest("North", 20, 3, false)));
    }
}

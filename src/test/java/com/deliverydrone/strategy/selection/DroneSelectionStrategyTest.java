package com.deliverydrone.strategy.selection;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.model.DroneOption;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DroneSelectionStrategyTest {
    private final DeliveryRequest request = new DeliveryRequest("North", 10, 3, false);
    private final List<DroneOption> drones = List.of(
            new DroneOption("Near", 1, 50, 5, 4),
            new DroneOption("Battery", 4, 95, 10, 2),
            new DroneOption("Free", 3, 70, 6, 0)
    );

    @Test
    void nearestStrategyChoosesClosestDrone() {
        assertEquals("Near", new NearestDroneStrategy().select(request, drones).getId());
    }

    @Test
    void highestBatteryStrategyChoosesMostChargedDrone() {
        assertEquals("Battery", new HighestBatteryDroneStrategy().select(request, drones).getId());
    }

    @Test
    void leastBusyStrategyChoosesFreeDrone() {
        assertEquals("Free", new LeastBusyDroneStrategy().select(request, drones).getId());
    }
}

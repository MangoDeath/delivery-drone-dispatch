package com.deliverydrone.strategy.selection;

import com.deliverydrone.strategy.model.DeliveryRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DroneSelectionSelectorTest {
    private final DroneSelectionSelector selector = new DroneSelectionSelector();

    @Test
    void choosesNearestDroneForUrgentRequest() {
        assertInstanceOf(NearestDroneStrategy.class,
                selector.selectFor(new DeliveryRequest("North", 10, 3, true)));
    }

    @Test
    void choosesHighestBatteryDroneForHeavyPayload() {
        assertInstanceOf(HighestBatteryDroneStrategy.class,
                selector.selectFor(new DeliveryRequest("North", 10, 8, false)));
    }

    @Test
    void choosesLeastBusyDroneForRegularRequest() {
        assertInstanceOf(LeastBusyDroneStrategy.class,
                selector.selectFor(new DeliveryRequest("North", 10, 3, false)));
    }
}

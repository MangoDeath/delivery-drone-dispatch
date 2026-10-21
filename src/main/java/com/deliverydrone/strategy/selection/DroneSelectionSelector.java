package com.deliverydrone.strategy.selection;

import com.deliverydrone.strategy.model.DeliveryRequest;

public class DroneSelectionSelector {
    private static final double HEAVY_PAYLOAD_KG = 5.0;

    public DroneSelectionStrategy selectFor(DeliveryRequest request) {
        if (request.isUrgent()) {
            return new NearestDroneStrategy();
        }
        if (request.getPayloadKg() > HEAVY_PAYLOAD_KG) {
            return new HighestBatteryDroneStrategy();
        }
        return new LeastBusyDroneStrategy();
    }
}

package com.deliverydrone.strategy.selection;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.model.DroneOption;

import java.util.Comparator;
import java.util.List;

public class LeastBusyDroneStrategy implements DroneSelectionStrategy {
    @Override
    public DroneOption select(DeliveryRequest request, List<DroneOption> drones) {
        return drones.stream()
                .filter(drone -> drone.getMaxPayloadKg() >= request.getPayloadKg())
                .min(Comparator.comparingInt(DroneOption::getActiveDeliveries))
                .orElseThrow(() -> new IllegalArgumentException("No suitable drone."));
    }
}

package com.deliverydrone.strategy.selection;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.model.DroneOption;

import java.util.Comparator;
import java.util.List;

public class HighestBatteryDroneStrategy implements DroneSelectionStrategy {
    @Override
    public DroneOption select(DeliveryRequest request, List<DroneOption> drones) {
        return drones.stream()
                .filter(drone -> drone.getMaxPayloadKg() >= request.getPayloadKg())
                .max(Comparator.comparingDouble(DroneOption::getBatteryPercent))
                .orElseThrow(() -> new IllegalArgumentException("No suitable drone."));
    }
}

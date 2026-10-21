package com.deliverydrone.strategy.selection;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.model.DroneOption;

import java.util.Comparator;
import java.util.List;

public class NearestDroneStrategy implements DroneSelectionStrategy {
    @Override
    public DroneOption select(DeliveryRequest request, List<DroneOption> drones) {
        return suitableDrones(request, drones)
                .min(Comparator.comparingDouble(DroneOption::getPickupDistanceKm))
                .orElseThrow(() -> new IllegalArgumentException("No suitable drone."));
    }

    private java.util.stream.Stream<DroneOption> suitableDrones(DeliveryRequest request,
                                                                  List<DroneOption> drones) {
        return drones.stream().filter(drone -> drone.getMaxPayloadKg() >= request.getPayloadKg());
    }
}

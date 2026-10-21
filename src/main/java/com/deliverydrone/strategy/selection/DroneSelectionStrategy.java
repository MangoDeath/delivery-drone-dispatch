package com.deliverydrone.strategy.selection;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.model.DroneOption;

import java.util.List;

public interface DroneSelectionStrategy {
    DroneOption select(DeliveryRequest request, List<DroneOption> drones);
}

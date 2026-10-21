package com.deliverydrone.strategy.selection;

import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.model.DroneOption;

import java.util.List;
import java.util.Objects;

public class DroneSelectionContext {
    private DroneSelectionStrategy strategy;

    public DroneSelectionContext(DroneSelectionStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(DroneSelectionStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy);
    }

    public DroneOption select(DeliveryRequest request, List<DroneOption> drones) {
        return strategy.select(request, drones);
    }
}

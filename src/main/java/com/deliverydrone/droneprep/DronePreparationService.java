package com.deliverydrone.droneprep;

import com.deliverydrone.shared.exception.DronePreparationException;

public class DronePreparationService {
    public void check(Drone drone, double payloadKg, double distanceKm) {
        if (payloadKg > drone.getMaxPayloadKg()) {
            throw new DronePreparationException("Payload " + payloadKg + " kg is above the drone limit of "
                    + drone.getMaxPayloadKg() + " kg.");
        }

        double neededPercent = distanceKm * 2;
        if (drone.getBatteryPercent() < neededPercent) {
            throw new DronePreparationException("Battery is " + drone.getBatteryPercent()
                    + "% but route needs " + neededPercent + "%.");
        }
    }
}

package com.deliverydrone.droneprep;

import com.deliverydrone.shared.exception.LowBatteryException;

public class BatteryManager {
    public void check(double distanceKm, Drone drone) {
        double neededPercent = distanceKm * 2;
        if (drone.getBatteryPercent() < neededPercent) {
            throw new LowBatteryException(drone.getBatteryPercent(), neededPercent);
        }
    }
}

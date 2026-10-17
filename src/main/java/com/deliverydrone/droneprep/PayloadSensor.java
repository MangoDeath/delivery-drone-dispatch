package com.deliverydrone.droneprep;

import com.deliverydrone.shared.exception.PayloadTooHeavyException;

public class PayloadSensor {
    public void check(double payloadKg, Drone drone) {
        if (payloadKg > drone.getMaxPayloadKg()) {
            throw new PayloadTooHeavyException(payloadKg, drone.getMaxPayloadKg());
        }
    }
}

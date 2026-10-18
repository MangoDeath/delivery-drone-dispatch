package com.deliverydrone.planning;

import com.deliverydrone.shared.exception.WeatherUnsafeException;

public class PlanningService {
    private static final double MAX_WIND_SPEED = 10.0;
    private static final double DRONE_SPEED_KM_PER_HOUR = 40.0;

    public FlightPlan createHeldPlan(String orderId, Zone zone) {
        if (zone.getWindSpeed() > MAX_WIND_SPEED) {
            throw new WeatherUnsafeException(zone.getWindSpeed());
        }
        double distanceKm = Math.hypot(zone.getX(), zone.getY());
        double travelTimeMinutes = distanceKm / DRONE_SPEED_KM_PER_HOUR * 60.0;
        return new FlightPlan(orderId, zone.getName(), distanceKm, travelTimeMinutes,
                FlightPlanStatus.HELD);
    }
}

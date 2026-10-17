package com.deliverydrone.planning;

import com.deliverydrone.shared.db.Database;

import java.sql.SQLException;

public class PlanningDemo {
    public static void main(String[] args) throws SQLException {
        FlightPlanRepository repository = new FlightPlanRepository(new Database("planning-demo.db"));
        repository.createTables();
        addDemoZoneIfNeeded(repository);
        createAndConfirmPlan(repository);
    }

    private static void addDemoZoneIfNeeded(FlightPlanRepository repository) throws SQLException {
        if (repository.findZone("North") == null) {
            repository.saveZone(new Zone("North", 3, 4, 6));
        }
    }

    private static void createAndConfirmPlan(FlightPlanRepository repository) throws SQLException {
        FlightPlan existingPlan = repository.findFlightPlan("demo-order");
        if (existingPlan != null) {
            printPlan(existingPlan);
            return;
        }
        Zone zone = repository.findZone("North");
        new WeatherChecker().check(zone);
        RoutePlanner planner = new RoutePlanner();
        FlightPlan plan = createHeldPlan(zone, planner);
        repository.saveFlightPlan(plan);
        plan.confirm();
        repository.updateFlightPlan(plan);
        printPlan(plan);
    }

    private static FlightPlan createHeldPlan(Zone zone, RoutePlanner planner) {
        double distanceKm = planner.calculateDistanceKm(zone);
        double travelTimeMinutes = planner.calculateTravelTimeMinutes(distanceKm);
        return new FlightPlan("demo-order", zone.getName(), distanceKm,
                travelTimeMinutes, FlightPlanStatus.HELD);
    }

    private static void printPlan(FlightPlan plan) {
        System.out.println("Flight plan saved for " + plan.getOrderId());
        System.out.println("Distance: " + plan.getDistanceKm() + " km");
        System.out.println("Travel time: " + plan.getTravelTimeMinutes() + " minutes");
        System.out.println("Status: " + plan.getStatus());
    }
}

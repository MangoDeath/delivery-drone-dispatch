package com.deliverydrone.strategy;

import com.deliverydrone.strategy.flighttime.ExpressFlightTimeStrategy;
import com.deliverydrone.strategy.flighttime.FlightTimeContext;
import com.deliverydrone.strategy.flighttime.FlightTimeSelector;
import com.deliverydrone.strategy.flighttime.StandardFlightTimeStrategy;
import com.deliverydrone.strategy.flighttime.WeatherFlightTimeStrategy;
import com.deliverydrone.strategy.model.DeliveryRequest;
import com.deliverydrone.strategy.model.DroneOption;
import com.deliverydrone.strategy.selection.DroneSelectionContext;
import com.deliverydrone.strategy.selection.DroneSelectionSelector;
import com.deliverydrone.strategy.selection.HighestBatteryDroneStrategy;
import com.deliverydrone.strategy.selection.LargestCapacityDroneStrategy;
import com.deliverydrone.strategy.selection.LeastBusyDroneStrategy;
import com.deliverydrone.strategy.selection.NearestDroneStrategy;
import com.deliverydrone.strategy.weather.SimulatedWindSource;
import com.deliverydrone.strategy.weather.WindSource;

import java.util.List;

public class StrategyDemo {
    public static void main(String[] args) {
        WindSource windSource = new SimulatedWindSource();
        DeliveryRequest request = new DeliveryRequest("Windy", 20, 3, false);
        List<DroneOption> drones = List.of(
                new DroneOption("Near", 1, 55, 5, 4),
                new DroneOption("Battery", 4, 95, 10, 2),
                new DroneOption("Free", 3, 70, 6, 0),
                new DroneOption("Capacity", 6, 60, 15, 5)
        );

        showDroneStrategies(request, drones);
        showFlightTimeStrategies(request, windSource);
    }

    private static void showDroneStrategies(DeliveryRequest request, List<DroneOption> drones) {
        System.out.println("Drone selection: same request, all strategies");
        System.out.println("Nearest: " + new NearestDroneStrategy().select(request, drones).getId());
        System.out.println("Highest battery: " + new HighestBatteryDroneStrategy().select(request, drones).getId());
        System.out.println("Least busy: " + new LeastBusyDroneStrategy().select(request, drones).getId());
        // ++++++++++++++++
        System.out.println("Largest capacity: " + new LargestCapacityDroneStrategy().select(request, drones).getId());

        DroneSelectionContext context = new DroneSelectionContext(new HighestBatteryDroneStrategy());
        System.out.println("Manual choice: " + context.select(request, drones).getId());
        context.setStrategy(new LeastBusyDroneStrategy());
        System.out.println("After runtime switch: " + context.select(request, drones).getId());

        DroneSelectionSelector selector = new DroneSelectionSelector();
        context.setStrategy(selector.selectFor(request));
        System.out.println("Automatic choice: " + context.select(request, drones).getId());
    }

    private static void showFlightTimeStrategies(DeliveryRequest request, WindSource windSource) {
        System.out.println("\nFlight time: same request, all strategies");
        System.out.println("Standard: " + new StandardFlightTimeStrategy().estimateMinutes(request));
        System.out.println("Express: " + new ExpressFlightTimeStrategy().estimateMinutes(request));
        System.out.println("Weather: " + new WeatherFlightTimeStrategy(windSource).estimateMinutes(request));

        FlightTimeContext context = new FlightTimeContext(new StandardFlightTimeStrategy());
        System.out.println("Manual choice: " + context.estimateMinutes(request));
        context.setStrategy(new ExpressFlightTimeStrategy());
        System.out.println("After runtime switch: " + context.estimateMinutes(request));

        FlightTimeSelector selector = new FlightTimeSelector(windSource);
        context.setStrategy(selector.selectFor(request));
        System.out.println("Automatic choice: " + context.estimateMinutes(request));
    }
}

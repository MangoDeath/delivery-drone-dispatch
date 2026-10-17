package com.deliverydrone.droneprep;

import com.deliverydrone.shared.db.Database;

import java.sql.SQLException;

public class DronePrepDemo {
    public static void main(String[] args) throws SQLException {
        DroneRepository repository = new DroneRepository(new Database("drone-prep-demo.db"));
        repository.createTables();
        addDemoDroneIfNeeded(repository);
        createAndLoadBooking(repository);
    }

    private static void addDemoDroneIfNeeded(DroneRepository repository) throws SQLException {
        if (repository.findDrone("D1") == null) {
            repository.saveDrone(new Drone("D1", 90, 5));
        }
    }

    private static void createAndLoadBooking(DroneRepository repository) throws SQLException {
        DroneBooking existingBooking = repository.findBooking("demo-order");
        if (existingBooking != null) {
            printBooking(existingBooking);
            return;
        }
        Drone drone = repository.findDrone("D1");
        new PayloadSensor().check(3, drone);
        new BatteryManager().check(5, drone);
        DroneBooking booking = new DroneBooking("demo-order", drone.getId(), 3,
                DroneBookingStatus.RESERVED);
        repository.saveBooking(booking);
        booking.load();
        repository.updateBooking(booking);
        printBooking(booking);
    }

    private static void printBooking(DroneBooking booking) {
        System.out.println("Booking saved for " + booking.getOrderId());
        System.out.println("Drone: " + booking.getDroneId());
        System.out.println("Payload: " + booking.getPayloadKg() + " kg");
        System.out.println("Status: " + booking.getStatus());
    }
}

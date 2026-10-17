package com.deliverydrone.droneprep;

import com.deliverydrone.shared.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DroneRepository {
    private final Database database;

    public DroneRepository(Database database) {
        this.database = database;
    }

    public void createTables() throws SQLException {
        execute("CREATE TABLE IF NOT EXISTS drones ("
                + "drone_id TEXT PRIMARY KEY, battery_percent REAL, max_payload_kg REAL)");
        execute("CREATE TABLE IF NOT EXISTS drone_bookings ("
                + "order_id TEXT PRIMARY KEY, drone_id TEXT, payload_kg REAL, status TEXT)");
    }

    public void saveDrone(Drone drone) throws SQLException {
        String sql = "INSERT INTO drones VALUES (?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, drone.getId());
            statement.setDouble(2, drone.getBatteryPercent());
            statement.setDouble(3, drone.getMaxPayloadKg());
            statement.executeUpdate();
        }
    }

    public Drone findDrone(String droneId) throws SQLException {
        String sql = "SELECT * FROM drones WHERE drone_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, droneId);
            return readDrone(statement);
        }
    }

    public List<Drone> findAllDrones() throws SQLException {
        List<Drone> drones = new ArrayList<>();
        String sql = "SELECT * FROM drones ORDER BY drone_id";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                drones.add(createDrone(result));
            }
        }
        return drones;
    }

    public void saveBooking(DroneBooking booking) throws SQLException {
        String sql = "INSERT INTO drone_bookings VALUES (?, ?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillBooking(statement, booking);
            statement.executeUpdate();
        }
    }

    public DroneBooking findBooking(String orderId) throws SQLException {
        String sql = "SELECT * FROM drone_bookings WHERE order_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderId);
            return readBooking(statement);
        }
    }

    public void updateBooking(DroneBooking booking) throws SQLException {
        String sql = "UPDATE drone_bookings SET status = ? WHERE order_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, booking.getStatus().name());
            statement.setString(2, booking.getOrderId());
            statement.executeUpdate();
        }
    }

    public List<DroneBooking> findAllBookings() throws SQLException {
        List<DroneBooking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM drone_bookings ORDER BY order_id";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                bookings.add(createBooking(result));
            }
        }
        return bookings;
    }

    private void execute(String sql) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    private Drone readDrone(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return createDrone(result);
        }
    }

    private Drone createDrone(ResultSet result) throws SQLException {
        return new Drone(result.getString("drone_id"), result.getDouble("battery_percent"),
                result.getDouble("max_payload_kg"));
    }

    private DroneBooking readBooking(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return createBooking(result);
        }
    }

    private DroneBooking createBooking(ResultSet result) throws SQLException {
        return new DroneBooking(result.getString("order_id"), result.getString("drone_id"),
                result.getDouble("payload_kg"),
                DroneBookingStatus.valueOf(result.getString("status")));
    }

    private void fillBooking(PreparedStatement statement, DroneBooking booking) throws SQLException {
        statement.setString(1, booking.getOrderId());
        statement.setString(2, booking.getDroneId());
        statement.setDouble(3, booking.getPayloadKg());
        statement.setString(4, booking.getStatus().name());
    }
}

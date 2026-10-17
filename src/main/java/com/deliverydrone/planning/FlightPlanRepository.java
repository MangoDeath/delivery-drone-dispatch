package com.deliverydrone.planning;

import com.deliverydrone.shared.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FlightPlanRepository {
    private final Database database;

    public FlightPlanRepository(Database database) {
        this.database = database;
    }

    public void createTables() throws SQLException {
        execute("CREATE TABLE IF NOT EXISTS planning_zones ("
                + "name TEXT PRIMARY KEY, x REAL, y REAL, wind_speed REAL)");
        execute("CREATE TABLE IF NOT EXISTS flight_plans ("
                + "order_id TEXT PRIMARY KEY, zone_name TEXT, distance_km REAL, "
                + "travel_time_minutes REAL, status TEXT)");
    }

    public void saveZone(Zone zone) throws SQLException {
        String sql = "INSERT INTO planning_zones VALUES (?, ?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, zone.getName());
            statement.setDouble(2, zone.getX());
            statement.setDouble(3, zone.getY());
            statement.setDouble(4, zone.getWindSpeed());
            statement.executeUpdate();
        }
    }

    public Zone findZone(String name) throws SQLException {
        String sql = "SELECT * FROM planning_zones WHERE name = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            return readZone(statement);
        }
    }

    public List<Zone> findAllZones() throws SQLException {
        List<Zone> zones = new ArrayList<>();
        String sql = "SELECT * FROM planning_zones ORDER BY name";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                zones.add(createZone(result));
            }
        }
        return zones;
    }

    public void saveFlightPlan(FlightPlan plan) throws SQLException {
        String sql = "INSERT INTO flight_plans VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillFlightPlan(statement, plan);
            statement.executeUpdate();
        }
    }

    public FlightPlan findFlightPlan(String orderId) throws SQLException {
        String sql = "SELECT * FROM flight_plans WHERE order_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderId);
            return readFlightPlan(statement);
        }
    }

    public void updateFlightPlan(FlightPlan plan) throws SQLException {
        String sql = "UPDATE flight_plans SET status = ? WHERE order_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, plan.getStatus().name());
            statement.setString(2, plan.getOrderId());
            statement.executeUpdate();
        }
    }

    public List<FlightPlan> findAllFlightPlans() throws SQLException {
        List<FlightPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM flight_plans ORDER BY order_id";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                plans.add(createFlightPlan(result));
            }
        }
        return plans;
    }

    private void execute(String sql) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    private Zone readZone(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return createZone(result);
        }
    }

    private Zone createZone(ResultSet result) throws SQLException {
        return new Zone(result.getString("name"), result.getDouble("x"),
                result.getDouble("y"), result.getDouble("wind_speed"));
    }

    private FlightPlan readFlightPlan(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return createFlightPlan(result);
        }
    }

    private FlightPlan createFlightPlan(ResultSet result) throws SQLException {
        return new FlightPlan(result.getString("order_id"), result.getString("zone_name"),
                result.getDouble("distance_km"), result.getDouble("travel_time_minutes"),
                FlightPlanStatus.valueOf(result.getString("status")));
    }

    private void fillFlightPlan(PreparedStatement statement, FlightPlan plan) throws SQLException {
        statement.setString(1, plan.getOrderId());
        statement.setString(2, plan.getZoneName());
        statement.setDouble(3, plan.getDistanceKm());
        statement.setDouble(4, plan.getTravelTimeMinutes());
        statement.setString(5, plan.getStatus().name());
    }
}

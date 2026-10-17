package com.deliverydrone.landing;

import com.deliverydrone.shared.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeliveryRepository {
    private final Database database;

    public DeliveryRepository(Database database) {
        this.database = database;
    }

    public void createTables() throws SQLException {
        execute("CREATE TABLE IF NOT EXISTS landing_pads ("
                + "pad_id TEXT PRIMARY KEY, zone_name TEXT, status TEXT)");
        execute("CREATE TABLE IF NOT EXISTS delivery_confirmations ("
                + "order_id TEXT PRIMARY KEY, pad_id TEXT, code TEXT, status TEXT)");
    }

    public void savePad(LandingPad pad) throws SQLException {
        String sql = "INSERT INTO landing_pads VALUES (?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pad.getId());
            statement.setString(2, pad.getZoneName());
            statement.setString(3, pad.getStatus().name());
            statement.executeUpdate();
        }
    }

    public LandingPad findPad(String padId) throws SQLException {
        String sql = "SELECT * FROM landing_pads WHERE pad_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, padId);
            return readPad(statement);
        }
    }

    public LandingPad findFreePad(String zoneName) throws SQLException {
        String sql = "SELECT * FROM landing_pads WHERE zone_name = ? AND status = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, zoneName);
            statement.setString(2, LandingPadStatus.FREE.name());
            return readPad(statement);
        }
    }

    public void updatePad(LandingPad pad) throws SQLException {
        String sql = "UPDATE landing_pads SET status = ? WHERE pad_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pad.getStatus().name());
            statement.setString(2, pad.getId());
            statement.executeUpdate();
        }
    }

    public List<LandingPad> findAllPads() throws SQLException {
        List<LandingPad> pads = new ArrayList<>();
        String sql = "SELECT * FROM landing_pads ORDER BY pad_id";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                pads.add(createPad(result));
            }
        }
        return pads;
    }

    public void saveConfirmation(DeliveryConfirmation confirmation) throws SQLException {
        String sql = "INSERT INTO delivery_confirmations VALUES (?, ?, ?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillConfirmation(statement, confirmation);
            statement.executeUpdate();
        }
    }

    public DeliveryConfirmation findConfirmation(String orderId) throws SQLException {
        String sql = "SELECT * FROM delivery_confirmations WHERE order_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderId);
            return readConfirmation(statement);
        }
    }

    public void updateConfirmation(DeliveryConfirmation confirmation) throws SQLException {
        String sql = "UPDATE delivery_confirmations SET status = ? WHERE order_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, confirmation.getStatus().name());
            statement.setString(2, confirmation.getOrderId());
            statement.executeUpdate();
        }
    }

    public List<DeliveryConfirmation> findAllConfirmations() throws SQLException {
        List<DeliveryConfirmation> confirmations = new ArrayList<>();
        String sql = "SELECT * FROM delivery_confirmations ORDER BY order_id";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                confirmations.add(createConfirmation(result));
            }
        }
        return confirmations;
    }

    public int countConfirmations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM delivery_confirmations";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getInt(1);
        }
    }

    private void execute(String sql) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    private LandingPad readPad(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return createPad(result);
        }
    }

    private LandingPad createPad(ResultSet result) throws SQLException {
        return new LandingPad(result.getString("pad_id"), result.getString("zone_name"),
                LandingPadStatus.valueOf(result.getString("status")));
    }

    private DeliveryConfirmation readConfirmation(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return createConfirmation(result);
        }
    }

    private DeliveryConfirmation createConfirmation(ResultSet result) throws SQLException {
        return new DeliveryConfirmation(result.getString("order_id"), result.getString("pad_id"),
                result.getString("code"),
                DeliveryConfirmationStatus.valueOf(result.getString("status")));
    }

    private void fillConfirmation(PreparedStatement statement, DeliveryConfirmation confirmation)
            throws SQLException {
        statement.setString(1, confirmation.getOrderId());
        statement.setString(2, confirmation.getPadId());
        statement.setString(3, confirmation.getCode());
        statement.setString(4, confirmation.getStatus().name());
    }
}

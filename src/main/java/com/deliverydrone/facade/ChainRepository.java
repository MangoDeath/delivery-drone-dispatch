package com.deliverydrone.facade;

import com.deliverydrone.shared.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChainRepository {
    private final Database database;

    public ChainRepository(Database database) {
        this.database = database;
    }

    public void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS dispatch_chains ("
                + "chain_id TEXT PRIMARY KEY, status TEXT)";
        execute(sql);
    }

    public void save(DispatchChain chain) throws SQLException {
        String sql = "INSERT INTO dispatch_chains VALUES (?, ?)";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chain.getChainId());
            statement.setString(2, chain.getStatus().name());
            statement.executeUpdate();
        }
    }

    public DispatchChain find(String chainId) throws SQLException {
        String sql = "SELECT * FROM dispatch_chains WHERE chain_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chainId);
            return readOne(statement);
        }
    }

    public void update(DispatchChain chain) throws SQLException {
        String sql = "UPDATE dispatch_chains SET status = ? WHERE chain_id = ?";
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chain.getStatus().name());
            statement.setString(2, chain.getChainId());
            statement.executeUpdate();
        }
    }

    public List<DispatchChain> findAll() throws SQLException {
        return readMany("SELECT * FROM dispatch_chains ORDER BY chain_id");
    }

    public List<DispatchChain> findPending() throws SQLException {
        String sql = "SELECT * FROM dispatch_chains WHERE status = 'PENDING_B' "
                + "OR status = 'PENDING_C' ORDER BY chain_id";
        return readMany(sql);
    }

    private void execute(String sql) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    private List<DispatchChain> readMany(String sql) throws SQLException {
        List<DispatchChain> chains = new ArrayList<>();
        try (Connection connection = database.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                chains.add(createChain(result));
            }
        }
        return chains;
    }

    private DispatchChain readOne(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                return null;
            }
            return createChain(result);
        }
    }

    private DispatchChain createChain(ResultSet result) throws SQLException {
        return new DispatchChain(result.getString("chain_id"),
                ChainStatus.valueOf(result.getString("status")));
    }
}

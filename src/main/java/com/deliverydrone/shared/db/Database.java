package com.deliverydrone.shared.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Database {
    private final String fileName;
    private final String url;

    public Database(String fileName) {
        this.fileName = fileName;
        this.url = "jdbc:sqlite:" + fileName;
    }

    public Connection openConnection() throws SQLException {
        // A new connection per operation is slower, but easier to explain in this small project.
        return DriverManager.getConnection(url);
    }

    public void reset() throws IOException {
        if (!":memory:".equals(fileName)) {
            Files.deleteIfExists(Path.of(fileName));
        }
    }
}

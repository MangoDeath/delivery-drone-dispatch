package com.deliverydrone.app;

import com.deliverydrone.shared.db.Database;

import java.sql.SQLException;

public class ConsoleApp {
    public static void main(String[] args) throws SQLException {
        Database database = new Database("delivery-drone.db");
        ConsoleMenu menu = new ConsoleMenu(database);
        menu.prepareDatabase();
        menu.run();
    }
}

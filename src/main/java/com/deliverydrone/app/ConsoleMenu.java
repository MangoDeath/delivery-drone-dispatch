package com.deliverydrone.app;

import com.deliverydrone.facade.AdminFacade;
import com.deliverydrone.facade.DispatchFacade;
import com.deliverydrone.shared.db.Database;

import java.sql.SQLException;
import java.util.Scanner;

public class ConsoleMenu {
    private final Database database;
    private final DispatchFacade dispatchFacade;
    private final AdminFacade adminFacade;
    private final DatabaseSeeder seeder;
    private final Scanner scanner;

    public ConsoleMenu(Database database) {
        this.database = database;
        dispatchFacade = new DispatchFacade(database);
        adminFacade = new AdminFacade(database);
        seeder = new DatabaseSeeder(database);
        scanner = new Scanner(System.in);
    }

    public void prepareDatabase() throws SQLException {
        dispatchFacade.createTables();
        seeder.seedIfNeeded();
    }

    public void run() {
        boolean running = true;
        while (running) {
            showMenu();
            if (!scanner.hasNextLine()) {
                return;
            }
            try {
                running = handle(scanner.nextLine());
            } catch (Exception exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            }
        }
    }

    private boolean handle(String choice) throws Exception {
        switch (choice) {
            case "1": dispatch(); break;
            case "2": adminFacade.showAllData(); break;
            case "3": adminFacade.showChainStatuses(); break;
            case "4": adminFacade.recoverPending(); break;
            case "5": resetDatabase(); break;
            case "6": leaveUnfinishedAfterB(); break;
            case "7": runAAndBThenChooseCompensation(); break;
            case "0": return false;
            default: System.out.println("Choose a number from the menu.");
        }
        return true;
    }

    private void dispatch() throws SQLException {
        System.out.print("Order ID: ");
        String orderId = scanner.nextLine();
        System.out.print("Drone ID (D1, D2, D3): ");
        String droneId = scanner.nextLine();
        System.out.print("Zone (North, East, Windy, BusyZone): ");
        String zone = scanner.nextLine();
        System.out.print("Payload kg: ");
        double payloadKg = Double.parseDouble(scanner.nextLine());
        dispatchFacade.dispatch(orderId, droneId, zone, payloadKg);
        System.out.println("Delivery dispatched. Chain ID is " + orderId + ".");
    }

    private void resetDatabase() throws Exception {
        database.reset();
        prepareDatabase();
        System.out.println("Database was reset and seed data was restored.");
    }

    private void leaveUnfinishedAfterB() throws SQLException {
        System.out.print("Order ID: ");
        String orderId = scanner.nextLine();
        System.out.print("Drone ID (D1, D2, D3): ");
        String droneId = scanner.nextLine();
        System.out.print("Zone (North, East, Windy, BusyZone): ");
        String zone = scanner.nextLine();
        System.out.print("Payload kg: ");
        double payloadKg = Double.parseDouble(scanner.nextLine());
        dispatchFacade.dispatchAAndBOnly(orderId, droneId, zone, payloadKg);
        System.out.println("Saved after B. Exit, restart, then use Recover pending.");
    }

    // ***** +++++ LIVE DEMO: the operator decides whether to compensate A + B. +++++ *****
    private void runAAndBThenChooseCompensation() throws SQLException {
        System.out.print("Order ID: ");
        String orderId = scanner.nextLine();
        System.out.print("Drone ID (D1, D2, D3): ");
        String droneId = scanner.nextLine();
        System.out.print("Zone (North, East, Windy, BusyZone): ");
        String zone = scanner.nextLine();
        System.out.print("Payload kg: ");
        double payloadKg = Double.parseDouble(scanner.nextLine());
        dispatchFacade.dispatchAAndBOnly(orderId, droneId, zone, payloadKg);

        System.out.print("Compensate A and B now? (yes/no): ");
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            adminFacade.compensate(orderId);
            System.out.println("A and B were compensated.");
        } else {
            System.out.println("Chain remains PENDING_C. Use Recover pending after a restart.");
        }
    }

    private void showMenu() {
        System.out.println("\n1 Dispatch  2 Show all  3 Show chains");
        System.out.println("4 Recover pending  5 Reset DB  6 Stop after B (demo)");
        System.out.println("7 Run A+B and choose rollback  0 Exit");
        System.out.print("Choose: ");
    }
}

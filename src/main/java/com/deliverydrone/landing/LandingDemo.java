package com.deliverydrone.landing;

import com.deliverydrone.shared.db.Database;

import java.sql.SQLException;

public class LandingDemo {
    public static void main(String[] args) throws SQLException {
        DeliveryRepository repository = new DeliveryRepository(new Database("landing-demo.db"));
        repository.createTables();
        addDemoPadIfNeeded(repository);
        reserveAndConfirm(repository);
    }

    private static void addDemoPadIfNeeded(DeliveryRepository repository) throws SQLException {
        if (repository.findPad("NORTH-1") == null) {
            repository.savePad(new LandingPad("NORTH-1", "North", LandingPadStatus.FREE));
        }
    }

    private static void reserveAndConfirm(DeliveryRepository repository) throws SQLException {
        if (repository.findConfirmation("demo-order") != null) {
            System.out.println("Delivery was already confirmed.");
            return;
        }
        LandingPad pad = new LandingPadController(repository).reservePad("North");
        String code = new ConfirmationCodeGenerator().generate(1001);
        DeliveryConfirmation confirmation = new DeliveryConfirmation("demo-order", pad.getId(), code,
                DeliveryConfirmationStatus.PENDING);
        repository.saveConfirmation(confirmation);
        confirmation.confirm();
        repository.updateConfirmation(confirmation);
        System.out.println(confirmation);
    }
}

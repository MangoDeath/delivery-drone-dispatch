package com.deliverydrone.app;

import com.deliverydrone.droneprep.Drone;
import com.deliverydrone.droneprep.DroneRepository;
import com.deliverydrone.landing.DeliveryRepository;
import com.deliverydrone.landing.LandingPad;
import com.deliverydrone.landing.LandingPadStatus;
import com.deliverydrone.planning.FlightPlanRepository;
import com.deliverydrone.planning.Zone;
import com.deliverydrone.shared.db.Database;

import java.sql.SQLException;

public class DatabaseSeeder {
    private final FlightPlanRepository planningRepository;
    private final DroneRepository droneRepository;
    private final DeliveryRepository deliveryRepository;

    public DatabaseSeeder(Database database) {
        planningRepository = new FlightPlanRepository(database);
        droneRepository = new DroneRepository(database);
        deliveryRepository = new DeliveryRepository(database);
    }

    public void seedIfNeeded() throws SQLException {
        if (planningRepository.findZone("North") != null) {
            return;
        }
        addZones();
        addDrones();
        addPads();
    }

    private void addZones() throws SQLException {
        planningRepository.saveZone(new Zone("North", 3, 4, 4));
        planningRepository.saveZone(new Zone("East", 6, 8, 6));
        planningRepository.saveZone(new Zone("Windy", 2, 2, 12));
        planningRepository.saveZone(new Zone("BusyZone", 1, 1, 3));
    }

    private void addDrones() throws SQLException {
        droneRepository.saveDrone(new Drone("D1", 90, 5));
        droneRepository.saveDrone(new Drone("D2", 60, 12));
        droneRepository.saveDrone(new Drone("D3", 10, 4));
    }

    private void addPads() throws SQLException {
        deliveryRepository.savePad(new LandingPad("N-1", "North", LandingPadStatus.FREE));
        deliveryRepository.savePad(new LandingPad("E-1", "East", LandingPadStatus.FREE));
        deliveryRepository.savePad(new LandingPad("E-2", "East", LandingPadStatus.FREE));
        deliveryRepository.savePad(new LandingPad("W-1", "Windy", LandingPadStatus.FREE));
        deliveryRepository.savePad(new LandingPad("B-1", "BusyZone", LandingPadStatus.BUSY));
        deliveryRepository.savePad(new LandingPad("B-2", "BusyZone", LandingPadStatus.BUSY));
    }
}

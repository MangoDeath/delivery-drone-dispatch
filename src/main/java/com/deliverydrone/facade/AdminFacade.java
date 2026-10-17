package com.deliverydrone.facade;

import com.deliverydrone.droneprep.DroneRepository;
import com.deliverydrone.landing.DeliveryRepository;
import com.deliverydrone.planning.FlightPlanRepository;
import com.deliverydrone.shared.db.Database;

import java.sql.SQLException;
import java.util.List;

public class AdminFacade {
    private final FlightPlanRepository planningRepository;
    private final DroneRepository droneRepository;
    private final DeliveryRepository deliveryRepository;
    private final ChainRepository chainRepository;
    private final CompensationService compensationService;

    public AdminFacade(Database database) {
        planningRepository = new FlightPlanRepository(database);
        droneRepository = new DroneRepository(database);
        deliveryRepository = new DeliveryRepository(database);
        chainRepository = new ChainRepository(database);
        compensationService = new CompensationService(chainRepository, planningRepository, droneRepository);
    }

    public void showAllData() throws SQLException {
        System.out.println("Zones: " + planningRepository.findAllZones());
        System.out.println("Flight plans: " + planningRepository.findAllFlightPlans());
        System.out.println("Drones: " + droneRepository.findAllDrones());
        System.out.println("Bookings: " + droneRepository.findAllBookings());
        System.out.println("Landing pads: " + deliveryRepository.findAllPads());
        System.out.println("Confirmations: " + deliveryRepository.findAllConfirmations());
        showChainStatuses();
    }

    public void showChainStatuses() throws SQLException {
        System.out.println("Dispatch chains: " + chainRepository.findAll());
    }

    public void compensate(String chainId) throws SQLException {
        compensationService.compensate(chainId);
    }

    public void recoverPending() throws SQLException {
        List<DispatchChain> pendingChains = chainRepository.findPending();
        for (DispatchChain chain : pendingChains) {
            compensationService.compensate(chain.getChainId());
        }
    }
}

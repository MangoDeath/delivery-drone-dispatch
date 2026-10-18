package com.deliverydrone.facade;

import com.deliverydrone.droneprep.Drone;
import com.deliverydrone.droneprep.DroneBooking;
import com.deliverydrone.droneprep.DroneBookingStatus;
import com.deliverydrone.droneprep.DronePreparationService;
import com.deliverydrone.droneprep.DroneRepository;
import com.deliverydrone.landing.DeliveryConfirmation;
import com.deliverydrone.landing.DeliveryConfirmationStatus;
import com.deliverydrone.landing.DeliveryRepository;
import com.deliverydrone.landing.LandingPad;
import com.deliverydrone.landing.LandingService;
import com.deliverydrone.planning.FlightPlan;
import com.deliverydrone.planning.FlightPlanRepository;
import com.deliverydrone.planning.PlanningService;
import com.deliverydrone.planning.Zone;
import com.deliverydrone.shared.db.Database;
import com.deliverydrone.shared.exception.NoFreeLandingPadException;

import java.sql.SQLException;

public class DispatchFacade {
    private final FlightPlanRepository planningRepository;
    private final DroneRepository droneRepository;
    private final DeliveryRepository deliveryRepository;
    private final ChainRepository chainRepository;
    private final CompensationService compensationService;

    public DispatchFacade(Database database) {
        planningRepository = new FlightPlanRepository(database);
        droneRepository = new DroneRepository(database);
        deliveryRepository = new DeliveryRepository(database);
        chainRepository = new ChainRepository(database);
        compensationService = new CompensationService(chainRepository, planningRepository, droneRepository);
    }

    public void createTables() throws SQLException {
        planningRepository.createTables();
        droneRepository.createTables();
        deliveryRepository.createTables();
        chainRepository.createTables();
    }

    public void dispatch(String orderId, String droneId, String zoneName, double payloadKg)
            throws SQLException {
        Zone zone = planningRepository.findZone(zoneName);
        checkZone(zone);
        FlightPlan plan = createFlightPlan(orderId, zone);
        DispatchChain chain = new DispatchChain(orderId, ChainStatus.PENDING_B);
        chainRepository.save(chain);
        try {
            createBooking(orderId, droneId, payloadKg, plan.getDistanceKm());
            chain.moveToPendingC();
            chainRepository.update(chain);
            createConfirmation(orderId, zoneName);
            chain.complete();
            chainRepository.update(chain);
        } catch (RuntimeException exception) {
            compensationService.compensate(orderId);
            throw exception;
        }
    }

    /**
     * Demo helper: persists A and B, then returns before C starts. It lets the
     * console demonstrate recovery after the process is restarted.
     */
    public void leaveUnfinishedAfterB(String orderId, String droneId, String zoneName, double payloadKg)
            throws SQLException {
        Zone zone = planningRepository.findZone(zoneName);
        checkZone(zone);
        FlightPlan plan = createFlightPlan(orderId, zone);
        DispatchChain chain = new DispatchChain(orderId, ChainStatus.PENDING_B);
        chainRepository.save(chain);
        createBooking(orderId, droneId, payloadKg, plan.getDistanceKm());
        chain.moveToPendingC();
        chainRepository.update(chain);
    }

    private void checkZone(Zone zone) {
        if (zone == null) {
            throw new IllegalArgumentException("Zone does not exist.");
        }
    }

    private FlightPlan createFlightPlan(String orderId, Zone zone) throws SQLException {
        FlightPlan plan = new PlanningService().createHeldPlan(orderId, zone);
        planningRepository.saveFlightPlan(plan);
        plan.confirm();
        planningRepository.updateFlightPlan(plan);
        return plan;
    }

    private void createBooking(String orderId, String droneId, double payloadKg, double distanceKm)
            throws SQLException {
        Drone drone = droneRepository.findDrone(droneId);
        if (drone == null) {
            throw new IllegalArgumentException("Drone does not exist.");
        }
        new DronePreparationService().check(drone, payloadKg, distanceKm);
        DroneBooking booking = new DroneBooking(orderId, droneId, payloadKg, DroneBookingStatus.RESERVED);
        droneRepository.saveBooking(booking);
        booking.load();
        droneRepository.updateBooking(booking);
    }

    private void createConfirmation(String orderId, String zoneName) throws SQLException {
        LandingPad pad = deliveryRepository.findFreePad(zoneName);
        if (pad == null) {
            throw new NoFreeLandingPadException(zoneName);
        }
        LandingService service = new LandingService();
        service.reserve(pad);
        deliveryRepository.updatePad(pad);
        int codeNumber = 1001 + deliveryRepository.countConfirmations();
        String code = service.createConfirmationCode(codeNumber);
        DeliveryConfirmation confirmation = new DeliveryConfirmation(orderId, pad.getId(), code,
                DeliveryConfirmationStatus.PENDING);
        deliveryRepository.saveConfirmation(confirmation);
        confirmation.confirm();
        deliveryRepository.updateConfirmation(confirmation);
    }
}

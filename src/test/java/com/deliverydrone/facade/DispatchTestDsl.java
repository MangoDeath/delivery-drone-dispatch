package com.deliverydrone.facade;

import com.deliverydrone.droneprep.Drone;
import com.deliverydrone.droneprep.DroneBooking;
import com.deliverydrone.droneprep.DroneBookingStatus;
import com.deliverydrone.droneprep.DroneRepository;
import com.deliverydrone.landing.DeliveryConfirmationStatus;
import com.deliverydrone.landing.DeliveryRepository;
import com.deliverydrone.landing.LandingPad;
import com.deliverydrone.landing.LandingPadStatus;
import com.deliverydrone.planning.FlightPlan;
import com.deliverydrone.planning.FlightPlanRepository;
import com.deliverydrone.planning.FlightPlanStatus;
import com.deliverydrone.planning.PlanningService;
import com.deliverydrone.planning.Zone;
import com.deliverydrone.shared.db.Database;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DispatchTestDsl {
    private final Database database;
    private DispatchFacade dispatchFacade;
    private AdminFacade adminFacade;
    private final FlightPlanRepository planningRepository;
    private final DroneRepository droneRepository;
    private final DeliveryRepository deliveryRepository;
    private final ChainRepository chainRepository;
    private String droneId;
    private double battery = 90;
    private double maxPayload = 5;
    private String zoneName;
    private double x = 3;
    private double y = 4;
    private double wind = 3;
    private boolean allPadsBusy;
    private String orderId;
    private RuntimeException failure;

    private DispatchTestDsl() throws Exception {
        Path path = Files.createTempFile("delivery-drone-test-", ".db");
        database = new Database(path.toString());
        dispatchFacade = new DispatchFacade(database);
        adminFacade = new AdminFacade(database);
        dispatchFacade.createTables();
        planningRepository = new FlightPlanRepository(database);
        droneRepository = new DroneRepository(database);
        deliveryRepository = new DeliveryRepository(database);
        chainRepository = new ChainRepository(database);
    }

    public static DispatchTestDsl given() throws Exception {
        return new DispatchTestDsl();
    }

    public DispatchTestDsl drone(String id) {
        droneId = id;
        return this;
    }

    public DispatchTestDsl withBattery(double percent) {
        battery = percent;
        return this;
    }

    public DispatchTestDsl andMaxPayload(double kg) {
        maxPayload = kg;
        return this;
    }

    public DispatchTestDsl andZone(String name) {
        zoneName = name;
        return this;
    }

    public DispatchTestDsl withWind(double speed) {
        wind = speed;
        return this;
    }

    public DispatchTestDsl withCoordinates(double newX, double newY) {
        x = newX;
        y = newY;
        return this;
    }

    public DispatchTestDsl withAllPadsBusy() {
        allPadsBusy = true;
        return this;
    }

    public DispatchTestDsl whenDispatching(String newOrderId) {
        orderId = newOrderId;
        return this;
    }

    public DispatchTestDsl withPayload(double payloadKg) throws Exception {
        saveConfiguredData();
        try {
            dispatchFacade.dispatch(orderId, droneId, zoneName, payloadKg);
        } catch (RuntimeException exception) {
            failure = exception;
        }
        return this;
    }

    public DispatchTestDsl whenLeftUnfinishedAfterB(String newOrderId) throws Exception {
        orderId = newOrderId;
        saveConfiguredData();
        saveConfirmedPlan();
        saveLoadedBooking();
        DispatchChain chain = new DispatchChain(orderId, ChainStatus.PENDING_B);
        chainRepository.save(chain);
        chain.moveToPendingC();
        chainRepository.update(chain);
        return this;
    }

    public DispatchTestDsl restart() {
        // Repository methods have closed their connections, so new facades simulate an app restart.
        dispatchFacade = new DispatchFacade(database);
        adminFacade = new AdminFacade(database);
        return this;
    }

    public DispatchTestDsl recoverPending() throws Exception {
        adminFacade.recoverPending();
        return this;
    }

    public DispatchTestDsl thenChainIs(ChainStatus expected) throws Exception {
        assertEquals(expected, chainRepository.find(orderId).getStatus());
        return this;
    }

    public DispatchTestDsl andFlightPlanIs(FlightPlanStatus expected) throws Exception {
        assertEquals(expected, planningRepository.findFlightPlan(orderId).getStatus());
        return this;
    }

    public DispatchTestDsl andBookingIs(DroneBookingStatus expected) throws Exception {
        assertEquals(expected, droneRepository.findBooking(orderId).getStatus());
        return this;
    }

    public DispatchTestDsl andConfirmationIs(DeliveryConfirmationStatus expected) throws Exception {
        assertEquals(expected, deliveryRepository.findConfirmation(orderId).getStatus());
        return this;
    }

    public DispatchTestDsl thenFailedWith(Class<? extends RuntimeException> type) {
        assertTrue(type.isInstance(failure));
        return this;
    }

    public DispatchTestDsl andNoFlightPlan() throws Exception {
        assertNull(planningRepository.findFlightPlan(orderId));
        return this;
    }

    public DispatchTestDsl andNoBooking() throws Exception {
        assertNull(droneRepository.findBooking(orderId));
        return this;
    }

    public DispatchTestDsl andNoConfirmation() throws Exception {
        assertNull(deliveryRepository.findConfirmation(orderId));
        return this;
    }

    public DispatchTestDsl andNoChain() throws Exception {
        assertNull(chainRepository.find(orderId));
        return this;
    }

    public DispatchTestDsl andPadsAreBusy() throws Exception {
        assertEquals(LandingPadStatus.BUSY, deliveryRepository.findPad("P1").getStatus());
        return this;
    }

    public DispatchTestDsl andShowAllData() throws Exception {
        adminFacade.showAllData();
        return this;
    }

    private void saveConfiguredData() throws Exception {
        droneRepository.saveDrone(new Drone(droneId, battery, maxPayload));
        planningRepository.saveZone(new Zone(zoneName, x, y, wind));
        LandingPadStatus status = allPadsBusy ? LandingPadStatus.BUSY : LandingPadStatus.FREE;
        deliveryRepository.savePad(new LandingPad("P1", zoneName, status));
    }

    private void saveConfirmedPlan() throws Exception {
        Zone zone = planningRepository.findZone(zoneName);
        FlightPlan plan = new PlanningService().createHeldPlan(orderId, zone);
        planningRepository.saveFlightPlan(plan);
        plan.confirm();
        planningRepository.updateFlightPlan(plan);
    }

    private void saveLoadedBooking() throws Exception {
        DroneBooking booking = new DroneBooking(orderId, droneId, 3, DroneBookingStatus.RESERVED);
        droneRepository.saveBooking(booking);
        booking.load();
        droneRepository.updateBooking(booking);
    }
}

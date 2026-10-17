package com.deliverydrone.facade;

import com.deliverydrone.droneprep.BatteryManager;
import com.deliverydrone.droneprep.Drone;
import com.deliverydrone.droneprep.DroneBooking;
import com.deliverydrone.droneprep.DroneBookingStatus;
import com.deliverydrone.droneprep.DroneRepository;
import com.deliverydrone.droneprep.PayloadSensor;
import com.deliverydrone.landing.ConfirmationCodeGenerator;
import com.deliverydrone.landing.DeliveryConfirmation;
import com.deliverydrone.landing.DeliveryConfirmationStatus;
import com.deliverydrone.landing.DeliveryRepository;
import com.deliverydrone.landing.LandingPad;
import com.deliverydrone.landing.LandingPadController;
import com.deliverydrone.planning.FlightPlan;
import com.deliverydrone.planning.FlightPlanRepository;
import com.deliverydrone.planning.FlightPlanStatus;
import com.deliverydrone.planning.RoutePlanner;
import com.deliverydrone.planning.WeatherChecker;
import com.deliverydrone.planning.Zone;
import com.deliverydrone.shared.db.Database;

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
        checkWeather(zone);
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

    private void checkWeather(Zone zone) {
        if (zone == null) {
            throw new IllegalArgumentException("Zone does not exist.");
        }
        new WeatherChecker().check(zone);
    }

    private FlightPlan createFlightPlan(String orderId, Zone zone) throws SQLException {
        RoutePlanner planner = new RoutePlanner();
        double distanceKm = planner.calculateDistanceKm(zone);
        double minutes = planner.calculateTravelTimeMinutes(distanceKm);
        FlightPlan plan = new FlightPlan(orderId, zone.getName(), distanceKm, minutes,
                FlightPlanStatus.HELD);
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
        new PayloadSensor().check(payloadKg, drone);
        new BatteryManager().check(distanceKm, drone);
        DroneBooking booking = new DroneBooking(orderId, droneId, payloadKg, DroneBookingStatus.RESERVED);
        droneRepository.saveBooking(booking);
        booking.load();
        droneRepository.updateBooking(booking);
    }

    private void createConfirmation(String orderId, String zoneName) throws SQLException {
        LandingPadController controller = new LandingPadController(deliveryRepository);
        LandingPad pad = controller.reservePad(zoneName);
        int codeNumber = 1001 + deliveryRepository.countConfirmations();
        String code = new ConfirmationCodeGenerator().generate(codeNumber);
        DeliveryConfirmation confirmation = new DeliveryConfirmation(orderId, pad.getId(), code,
                DeliveryConfirmationStatus.PENDING);
        deliveryRepository.saveConfirmation(confirmation);
        confirmation.confirm();
        deliveryRepository.updateConfirmation(confirmation);
    }
}

package com.deliverydrone.facade;

import com.deliverydrone.planning.FlightPlan;
import com.deliverydrone.planning.FlightPlanStatus;
import com.deliverydrone.droneprep.DroneBooking;
import com.deliverydrone.droneprep.DroneBookingStatus;
import com.deliverydrone.landing.DeliveryConfirmation;
import com.deliverydrone.landing.DeliveryConfirmationStatus;
import com.deliverydrone.landing.LandingPad;
import com.deliverydrone.landing.LandingPadStatus;
import com.deliverydrone.shared.exception.IllegalStateTransitionException;
import com.deliverydrone.shared.exception.LowBatteryException;
import com.deliverydrone.shared.exception.NoFreeLandingPadException;
import com.deliverydrone.shared.exception.PayloadTooHeavyException;
import com.deliverydrone.shared.exception.WeatherUnsafeException;
import org.junit.jupiter.api.Test;

import static com.deliverydrone.facade.DispatchTestDsl.given;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DispatchFacadeTest {
    @Test
    void dispatchesHappyPath() throws Exception {
        given().drone("D1").withBattery(90).andZone("North")
                .whenDispatching("order-1").withPayload(3)
                .thenChainIs(ChainStatus.COMPLETED)
                .andFlightPlanIs(FlightPlanStatus.CONFIRMED)
                .andBookingIs(com.deliverydrone.droneprep.DroneBookingStatus.LOADED)
                .andConfirmationIs(com.deliverydrone.landing.DeliveryConfirmationStatus.CONFIRMED)
                .andShowAllData();
    }

    @Test
    void compensatesAWhenPayloadFailsInB() throws Exception {
        given().drone("D1").withBattery(90).andZone("North")
                .whenDispatching("order-2").withPayload(8)
                .thenFailedWith(PayloadTooHeavyException.class)
                .thenChainIs(ChainStatus.COMPENSATED)
                .andFlightPlanIs(FlightPlanStatus.CANCELLED)
                .andNoBooking().andNoConfirmation();
    }

    @Test
    void compensatesBThenAWhenLandingFailsInC() throws Exception {
        given().drone("D1").withBattery(90).andZone("BusyZone").withAllPadsBusy()
                .whenDispatching("order-3").withPayload(3)
                .thenFailedWith(NoFreeLandingPadException.class)
                .thenChainIs(ChainStatus.COMPENSATED)
                .andFlightPlanIs(FlightPlanStatus.CANCELLED)
                .andBookingIs(com.deliverydrone.droneprep.DroneBookingStatus.RELEASED)
                .andNoConfirmation().andPadsAreBusy();
    }

    @Test
    void savesNothingWhenWeatherFails() throws Exception {
        given().drone("D1").withBattery(90).andZone("Windy").withWind(12)
                .whenDispatching("order-4").withPayload(3)
                .thenFailedWith(WeatherUnsafeException.class)
                .andNoFlightPlan().andNoBooking().andNoConfirmation().andNoChain();
    }

    @Test
    void compensatesAWhenBatteryFailsInB() throws Exception {
        given().drone("D3").withBattery(10).andZone("East").withCoordinates(6, 8)
                .whenDispatching("order-low-battery").withPayload(3)
                .thenFailedWith(LowBatteryException.class)
                .thenChainIs(ChainStatus.COMPENSATED)
                .andFlightPlanIs(FlightPlanStatus.CANCELLED)
                .andNoBooking().andNoConfirmation();
    }

    @Test
    void rejectsIllegalStatusTransition() {
        FlightPlan plan = new FlightPlan("order-5", "North", 5, 7.5, FlightPlanStatus.HELD);
        plan.confirm();
        assertThrows(IllegalStateTransitionException.class, plan::confirm);
    }

    @Test
    void entitiesProtectTheirOwnStatusRules() {
        DroneBooking booking = new DroneBooking("b", "D1", 1, DroneBookingStatus.RESERVED);
        booking.load();
        booking.release();
        assertThrows(IllegalStateTransitionException.class, booking::release);

        LandingPad pad = new LandingPad("P", "North", LandingPadStatus.FREE);
        pad.markBusy();
        pad.free();
        assertThrows(IllegalStateTransitionException.class, pad::free);

        DeliveryConfirmation confirmation = new DeliveryConfirmation("c", "P", "DLV-1",
                DeliveryConfirmationStatus.PENDING);
        confirmation.confirm();
        confirmation.cancel();
        assertThrows(IllegalStateTransitionException.class, confirmation::cancel);
    }

    @Test
    void recoversPendingChainAfterRestart() throws Exception {
        given().drone("D1").withBattery(90).andZone("North")
                .whenLeftUnfinishedAfterB("order-6")
                .restart().recoverPending()
                .thenChainIs(ChainStatus.COMPENSATED)
                .andFlightPlanIs(FlightPlanStatus.CANCELLED)
                .andBookingIs(com.deliverydrone.droneprep.DroneBookingStatus.RELEASED);
    }
}

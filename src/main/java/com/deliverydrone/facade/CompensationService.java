package com.deliverydrone.facade;

import com.deliverydrone.droneprep.DroneBooking;
import com.deliverydrone.droneprep.DroneBookingStatus;
import com.deliverydrone.droneprep.DroneRepository;
import com.deliverydrone.planning.FlightPlan;
import com.deliverydrone.planning.FlightPlanRepository;
import com.deliverydrone.planning.FlightPlanStatus;

import java.sql.SQLException;

class CompensationService {
    private final ChainRepository chainRepository;
    private final FlightPlanRepository flightPlanRepository;
    private final DroneRepository droneRepository;

    CompensationService(ChainRepository chainRepository, FlightPlanRepository flightPlanRepository,
                        DroneRepository droneRepository) {
        this.chainRepository = chainRepository;
        this.flightPlanRepository = flightPlanRepository;
        this.droneRepository = droneRepository;
    }

    void compensate(String chainId) throws SQLException {
        DispatchChain chain = chainRepository.find(chainId);
        if (chain == null || chain.getStatus() == ChainStatus.COMPENSATED
                || chain.getStatus() == ChainStatus.COMPLETED) {
            return;
        }
        if (chain.getStatus() == ChainStatus.PENDING_C) {
            releaseBooking(chainId);
        }
        cancelFlightPlan(chainId);
        chain.compensate();
        chainRepository.update(chain);
    }

    private void releaseBooking(String chainId) throws SQLException {
        DroneBooking booking = droneRepository.findBooking(chainId);
        if (booking != null && booking.getStatus() != DroneBookingStatus.RELEASED) {
            booking.release();
            droneRepository.updateBooking(booking);
        }
    }

    private void cancelFlightPlan(String chainId) throws SQLException {
        FlightPlan plan = flightPlanRepository.findFlightPlan(chainId);
        if (plan != null && plan.getStatus() != FlightPlanStatus.CANCELLED) {
            plan.cancel();
            flightPlanRepository.updateFlightPlan(plan);
        }
    }
}

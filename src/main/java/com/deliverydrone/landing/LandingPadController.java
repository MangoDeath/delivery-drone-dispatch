package com.deliverydrone.landing;

import com.deliverydrone.shared.exception.NoFreeLandingPadException;

import java.sql.SQLException;

public class LandingPadController {
    private final DeliveryRepository repository;

    public LandingPadController(DeliveryRepository repository) {
        this.repository = repository;
    }

    public LandingPad reservePad(String zoneName) throws SQLException {
        LandingPad pad = repository.findFreePad(zoneName);
        if (pad == null) {
            throw new NoFreeLandingPadException(zoneName);
        }
        pad.markBusy();
        repository.updatePad(pad);
        return pad;
    }

    public void freePad(String padId) throws SQLException {
        LandingPad pad = repository.findPad(padId);
        if (pad != null && pad.getStatus() == LandingPadStatus.BUSY) {
            // This condition keeps recovery safe if a pad was already freed.
            pad.free();
            repository.updatePad(pad);
        }
    }
}

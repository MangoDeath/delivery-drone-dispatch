package com.deliverydrone.landing;

import com.deliverydrone.shared.exception.IllegalStateTransitionException;

public class LandingPad {
    private final String id;
    private final String zoneName;
    private LandingPadStatus status;

    public LandingPad(String id, String zoneName, LandingPadStatus status) {
        this.id = id;
        this.zoneName = zoneName;
        this.status = status;
    }

    public void markBusy() {
        if (status != LandingPadStatus.FREE) {
            throw new IllegalStateTransitionException("LandingPad", status.name(),
                    LandingPadStatus.BUSY.name());
        }
        status = LandingPadStatus.BUSY;
    }

    public void free() {
        if (status != LandingPadStatus.BUSY) {
            throw new IllegalStateTransitionException("LandingPad", status.name(),
                    LandingPadStatus.FREE.name());
        }
        status = LandingPadStatus.FREE;
    }

    public String getId() {
        return id;
    }

    public String getZoneName() {
        return zoneName;
    }

    public LandingPadStatus getStatus() {
        return status;
    }

    public String toString() {
        return id + " in " + zoneName + " is " + status;
    }
}

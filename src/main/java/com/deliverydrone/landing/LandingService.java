package com.deliverydrone.landing;

public class LandingService {
    public void reserve(LandingPad pad) {
        pad.markBusy();
    }

    public String createConfirmationCode(int number) {
        return "DLV-" + number;
    }
}

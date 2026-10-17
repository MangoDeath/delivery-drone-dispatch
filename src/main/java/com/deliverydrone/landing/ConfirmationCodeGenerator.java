package com.deliverydrone.landing;

public class ConfirmationCodeGenerator {
    public String generate(int number) {
        return "DLV-" + number;
    }
}

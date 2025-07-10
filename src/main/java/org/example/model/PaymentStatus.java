package org.example.model;

public enum PaymentStatus {
    PENDING("Oczekująca"),
    PAID("Opłacona"),
    FAILED("Nieudana");

    private final String name;

    PaymentStatus(String name) {
        this.name = name;
    }
    public String getName(){
        return name;
    }

}

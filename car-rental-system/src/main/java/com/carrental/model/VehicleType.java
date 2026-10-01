package com.carrental.model;

public enum VehicleType {
    ECONOMY("Economy"),
    SUV("SUV"),
    LUXURY("Luxury");

    private final String label;

    VehicleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

package com.carrental.model;

import java.math.BigDecimal;

public class Vehicle {
    private final String id;
    private final String make;
    private final String model;
    private final VehicleType type;
    private final BigDecimal dailyRate;

    public Vehicle(String id, String make, String model, VehicleType type, BigDecimal dailyRate) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.type = type;
        this.dailyRate = dailyRate;
    }

    public String getId() { return id; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public VehicleType getType() { return type; }
    public BigDecimal getDailyRate() { return dailyRate; }

    @Override
    public String toString() {
        return String.format("%-6s %-8s %-10s %-10s $%s/day", id, type.getLabel(), make, model, dailyRate);
    }
}

package com.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Booking {
    private final String id;
    private final Vehicle vehicle;
    private final Customer customer;
    private final LocalDate startDate;
    private final LocalDate endDate;      // exclusive: the day the car is returned
    private final BigDecimal baseCost;
    private BigDecimal lateFee = BigDecimal.ZERO.setScale(2);
    private BookingStatus status = BookingStatus.CONFIRMED;

    public Booking(String id, Vehicle vehicle, Customer customer,
                   LocalDate startDate, LocalDate endDate, BigDecimal baseCost) {
        this.id = id;
        this.vehicle = vehicle;
        this.customer = customer;
        this.startDate = startDate;
        this.endDate = endDate;
        this.baseCost = baseCost;
    }

    /** Two date ranges [start, end) overlap if each starts before the other ends. */
    public boolean overlaps(LocalDate otherStart, LocalDate otherEnd) {
        return status == BookingStatus.CONFIRMED
                && startDate.isBefore(otherEnd)
                && otherStart.isBefore(endDate);
    }

    public BigDecimal getTotalCharged() {
        return baseCost.add(lateFee);
    }

    public String getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public Customer getCustomer() { return customer; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getBaseCost() { return baseCost; }
    public BigDecimal getLateFee() { return lateFee; }
    public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("%-5s %-6s %s -> %s  $%s  [%s]",
                id, vehicle.getId(), startDate, endDate, getTotalCharged(), status);
    }
}

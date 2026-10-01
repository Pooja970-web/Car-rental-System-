package com.carrental.exception;

public class VehicleUnavailableException extends RentalException {
    public VehicleUnavailableException(String vehicleId) {
        super("Vehicle " + vehicleId + " is not available for the selected dates.");
    }
}

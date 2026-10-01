package com.carrental.exception;

/** Base class for all business-rule violations in the rental system. */
public class RentalException extends RuntimeException {
    public RentalException(String message) {
        super(message);
    }
}

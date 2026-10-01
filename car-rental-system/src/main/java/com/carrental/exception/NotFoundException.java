package com.carrental.exception;

public class NotFoundException extends RentalException {
    public NotFoundException(String entity, String id) {
        super(entity + " not found: " + id);
    }
}

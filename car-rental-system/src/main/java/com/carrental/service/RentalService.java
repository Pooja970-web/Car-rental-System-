package com.carrental.service;

import com.carrental.exception.NotFoundException;
import com.carrental.exception.RentalException;
import com.carrental.exception.VehicleUnavailableException;
import com.carrental.model.*;
import com.carrental.pricing.PricingStrategy;
import com.carrental.repository.InMemoryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RentalService {
    private static final BigDecimal LATE_FEE_MULTIPLIER = new BigDecimal("1.5");

    private final InMemoryRepository<Vehicle> vehicles = new InMemoryRepository<>(Vehicle::getId);
    private final InMemoryRepository<Customer> customers = new InMemoryRepository<>(Customer::getId);
    private final InMemoryRepository<Booking> bookings = new InMemoryRepository<>(Booking::getId);

    private final AtomicInteger customerSeq = new AtomicInteger();
    private final AtomicInteger bookingSeq = new AtomicInteger();

    private final PricingStrategy pricing;
    private final Clock clock; // injected so tests can control "today"

    public RentalService(PricingStrategy pricing, Clock clock) {
        this.pricing = pricing;
        this.clock = clock;
    }

    // ---------- setup ----------
    public void addVehicle(Vehicle vehicle) {
        vehicles.save(vehicle);
    }

    public Customer registerCustomer(String name, String email) {
        if (name == null || name.isBlank()) throw new RentalException("Name is required.");
        if (email == null || !email.contains("@")) throw new RentalException("A valid email is required.");
        boolean exists = customers.findAll().stream()
                .anyMatch(c -> c.getEmail().equalsIgnoreCase(email));
        if (exists) throw new RentalException("A customer with this email already exists.");

        Customer customer = new Customer("C" + customerSeq.incrementAndGet(), name.trim(), email.trim());
        customers.save(customer);
        return customer;
    }

    // ---------- queries ----------
    public List<Vehicle> getAllVehicles() {
        return vehicles.findAll();
    }

    public List<Vehicle> findAvailable(LocalDate start, LocalDate end, Optional<VehicleType> type) {
        validateDates(start, end);
        return vehicles.findAll().stream()
                .filter(v -> type.map(t -> v.getType() == t).orElse(true))
                .filter(v -> isAvailable(v, start, end))
                .sorted(Comparator.comparing(Vehicle::getDailyRate))
                .collect(Collectors.toList());
    }

    public List<Booking> getBookingsFor(String customerId) {
        return bookings.findAll().stream()
                .filter(b -> b.getCustomer().getId().equals(customerId))
                .collect(Collectors.toList());
    }

    public Map<VehicleType, BigDecimal> revenueByType() {
        return bookings.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        b -> b.getVehicle().getType(),
                        () -> new EnumMap<>(VehicleType.class),
                        Collectors.reducing(BigDecimal.ZERO, Booking::getTotalCharged, BigDecimal::add)));
    }

    // ---------- commands ----------
    /** synchronized so two threads can't double-book the same car. */
    public synchronized Booking book(String customerId, String vehicleId, LocalDate start, LocalDate end) {
        validateDates(start, end);
        if (start.isBefore(LocalDate.now(clock))) {
            throw new RentalException("Start date cannot be in the past.");
        }
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer", customerId));
        Vehicle vehicle = vehicles.findById(vehicleId)
                .orElseThrow(() -> new NotFoundException("Vehicle", vehicleId));

        if (!isAvailable(vehicle, start, end)) {
            throw new VehicleUnavailableException(vehicleId);
        }

        BigDecimal cost = pricing.calculate(vehicle, start, end);
        Booking booking = new Booking("B" + bookingSeq.incrementAndGet(), vehicle, customer, start, end, cost);
        bookings.save(booking);
        return booking;
    }

    public synchronized Booking cancel(String bookingId) {
        Booking booking = requireConfirmed(bookingId);
        booking.setStatus(BookingStatus.CANCELLED);
        return booking;
    }

    /** Completes a rental. Returning after the end date costs 1.5x the daily rate per extra day. */
    public synchronized Booking returnVehicle(String bookingId, LocalDate returnDate) {
        Booking booking = requireConfirmed(bookingId);
        if (returnDate.isBefore(booking.getStartDate())) {
            throw new RentalException("Return date is before the rental started.");
        }
        long lateDays = ChronoUnit.DAYS.between(booking.getEndDate(), returnDate);
        if (lateDays > 0) {
            BigDecimal fee = booking.getVehicle().getDailyRate()
                    .multiply(LATE_FEE_MULTIPLIER)
                    .multiply(BigDecimal.valueOf(lateDays))
                    .setScale(2, RoundingMode.HALF_UP);
            booking.setLateFee(fee);
        }
        booking.setStatus(BookingStatus.COMPLETED);
        return booking;
    }

    // ---------- helpers ----------
    private boolean isAvailable(Vehicle vehicle, LocalDate start, LocalDate end) {
        return bookings.findAll().stream()
                .filter(b -> b.getVehicle().getId().equals(vehicle.getId()))
                .noneMatch(b -> b.overlaps(start, end));
    }

    private Booking requireConfirmed(String bookingId) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RentalException("Booking " + bookingId + " is already " + booking.getStatus() + ".");
        }
        return booking;
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (!end.isAfter(start)) {
            throw new RentalException("End date must be after start date.");
        }
    }
}

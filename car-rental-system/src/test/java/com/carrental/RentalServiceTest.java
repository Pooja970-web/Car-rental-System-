package com.carrental;

import com.carrental.exception.RentalException;
import com.carrental.exception.VehicleUnavailableException;
import com.carrental.model.*;
import com.carrental.pricing.*;
import com.carrental.service.RentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RentalServiceTest {
    // Fixed "today": Thursday 2026-10-01
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-01T00:00:00Z"), ZoneOffset.UTC);
    private RentalService service;
    private Customer alice;

    @BeforeEach
    void setUp() {
        service = new RentalService(new StandardPricing(), clock);
        service.addVehicle(new Vehicle("V1", "Toyota", "Yaris", VehicleType.ECONOMY, new BigDecimal("100.00")));
        service.addVehicle(new Vehicle("V2", "BMW", "5 Series", VehicleType.LUXURY, new BigDecimal("200.00")));
        alice = service.registerCustomer("Alice", "alice@example.com");
    }

    @Test
    void bookingCalculatesCost() {
        Booking b = service.book(alice.getId(), "V1", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 8));
        assertEquals(new BigDecimal("300.00"), b.getTotalCharged());
        assertEquals(BookingStatus.CONFIRMED, b.getStatus());
    }

    @Test
    void overlappingBookingIsRejected() {
        service.book(alice.getId(), "V1", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 8));
        assertThrows(VehicleUnavailableException.class, () ->
                service.book(alice.getId(), "V1", LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 10)));
    }

    @Test
    void backToBackBookingsAreAllowed() {
        service.book(alice.getId(), "V1", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 8));
        assertDoesNotThrow(() ->
                service.book(alice.getId(), "V1", LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 10)));
    }

    @Test
    void cancellingFreesTheVehicle() {
        Booking b = service.book(alice.getId(), "V1", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 8));
        service.cancel(b.getId());
        assertEquals(2, service.findAvailable(
                LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 8), Optional.empty()).size());
    }

    @Test
    void pastStartDateIsRejected() {
        assertThrows(RentalException.class, () ->
                service.book(alice.getId(), "V1", LocalDate.of(2026, 9, 28), LocalDate.of(2026, 10, 2)));
    }

    @Test
    void endBeforeStartIsRejected() {
        assertThrows(RentalException.class, () ->
                service.book(alice.getId(), "V1", LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 5)));
    }

    @Test
    void lateReturnChargesOnePointFiveTimesDailyRate() {
        Booking b = service.book(alice.getId(), "V1", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 8));
        service.returnVehicle(b.getId(), LocalDate.of(2026, 10, 10)); // 2 days late
        assertEquals(new BigDecimal("300.00"), b.getLateFee());
        assertEquals(new BigDecimal("600.00"), b.getTotalCharged());
    }

    @Test
    void duplicateEmailIsRejected() {
        assertThrows(RentalException.class, () -> service.registerCustomer("Al", "ALICE@example.com"));
    }

    @Test
    void weekendSurchargeAppliesToSaturdayAndSunday() {
        PricingStrategy p = new WeekendSurchargePricing(new StandardPricing());
        Vehicle v = new Vehicle("X", "A", "B", VehicleType.ECONOMY, new BigDecimal("100.00"));
        // Fri, Sat, Sun = 300 + 2 * 20
        assertEquals(new BigDecimal("340.00"),
                p.calculate(v, LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 5)));
    }

    @Test
    void longTermDiscountApplies() {
        PricingStrategy p = new LongTermDiscountPricing(new StandardPricing());
        Vehicle v = new Vehicle("X", "A", "B", VehicleType.ECONOMY, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("630.00"),
                p.calculate(v, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 12)));   // 7 days, 10%
        assertEquals(new BigDecimal("1190.00"),
                p.calculate(v, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 19)));   // 14 days, 15%
    }
}

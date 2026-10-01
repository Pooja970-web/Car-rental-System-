package com.carrental;

import com.carrental.exception.RentalException;
import com.carrental.model.*;
import com.carrental.pricing.*;
import com.carrental.service.RentalService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        // Decorators stack: standard price -> + weekend surcharge -> - long-term discount
        PricingStrategy pricing = new LongTermDiscountPricing(
                new WeekendSurchargePricing(new StandardPricing()));
        RentalService service = new RentalService(pricing, Clock.systemDefaultZone());
        seedData(service);

        System.out.println("=== Car Rental System ===");
        System.out.println("Demo customer available: C1 (Demo User)");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("Choose an option");
            try {
                switch (choice) {
                    case "1" -> service.getAllVehicles().forEach(System.out::println);
                    case "2" -> searchAvailable(service);
                    case "3" -> registerCustomer(service);
                    case "4" -> book(service);
                    case "5" -> cancel(service);
                    case "6" -> returnVehicle(service);
                    case "7" -> service.getBookingsFor(prompt("Customer ID")).forEach(System.out::println);
                    case "8" -> revenueReport(service);
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (RentalException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("Error: dates must look like 2026-10-15.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: invalid vehicle type. Use ECONOMY, SUV or LUXURY.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("""

                1. List all vehicles
                2. Search available vehicles
                3. Register customer
                4. Book a vehicle
                5. Cancel a booking
                6. Return a vehicle
                7. View my bookings
                8. Revenue report
                0. Exit""");
    }

    private static void searchAvailable(RentalService service) {
        LocalDate start = LocalDate.parse(prompt("Start date (yyyy-mm-dd)"));
        LocalDate end = LocalDate.parse(prompt("End date (yyyy-mm-dd)"));
        String type = prompt("Type (ECONOMY/SUV/LUXURY or blank for any)");
        Optional<VehicleType> filter = type.isBlank()
                ? Optional.empty() : Optional.of(VehicleType.valueOf(type.toUpperCase()));
        var results = service.findAvailable(start, end, filter);
        if (results.isEmpty()) System.out.println("No vehicles available.");
        else results.forEach(System.out::println);
    }

    private static void registerCustomer(RentalService service) {
        Customer c = service.registerCustomer(prompt("Name"), prompt("Email"));
        System.out.println("Registered: " + c);
    }

    private static void book(RentalService service) {
        Booking b = service.book(
                prompt("Customer ID"), prompt("Vehicle ID"),
                LocalDate.parse(prompt("Start date (yyyy-mm-dd)")),
                LocalDate.parse(prompt("End date (yyyy-mm-dd)")));
        System.out.println("Booked! " + b);
    }

    private static void cancel(RentalService service) {
        System.out.println("Cancelled: " + service.cancel(prompt("Booking ID")));
    }

    private static void returnVehicle(RentalService service) {
        Booking b = service.returnVehicle(prompt("Booking ID"),
                LocalDate.parse(prompt("Return date (yyyy-mm-dd)")));
        System.out.println("Returned: " + b);
        if (b.getLateFee().signum() > 0) System.out.println("Late fee charged: $" + b.getLateFee());
    }

    private static void revenueReport(RentalService service) {
        Map<VehicleType, BigDecimal> report = service.revenueByType();
        if (report.isEmpty()) System.out.println("No completed rentals yet.");
        report.forEach((type, total) -> System.out.printf("%-8s $%s%n", type.getLabel(), total));
    }

    private static String prompt(String label) {
        System.out.print(label + ": ");
        return in.nextLine().trim();
    }

    private static void seedData(RentalService s) {
        s.addVehicle(new Vehicle("V1", "Toyota", "Yaris", VehicleType.ECONOMY, new BigDecimal("35.00")));
        s.addVehicle(new Vehicle("V2", "Honda", "Civic", VehicleType.ECONOMY, new BigDecimal("42.00")));
        s.addVehicle(new Vehicle("V3", "Ford", "Explorer", VehicleType.SUV, new BigDecimal("75.00")));
        s.addVehicle(new Vehicle("V4", "Jeep", "Wrangler", VehicleType.SUV, new BigDecimal("85.00")));
        s.addVehicle(new Vehicle("V5", "BMW", "5 Series", VehicleType.LUXURY, new BigDecimal("140.00")));
        s.addVehicle(new Vehicle("V6", "Mercedes", "S-Class", VehicleType.LUXURY, new BigDecimal("190.00")));
        s.registerCustomer("Demo User", "demo@example.com");
    }
}

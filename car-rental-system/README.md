# Car Rental System

A console-based car rental application in **core Java 17**, built with clean layering, design patterns and unit tests.

## Features
- Browse the fleet and search vehicles available for a date range (filter by type, sorted by price)
- Register customers (email validation, duplicate detection)
- Book, cancel and return vehicles with **date-overlap detection** (no double-booking)
- Dynamic pricing: weekend surcharge (+20%) and long-term discounts (10% at 7+ days, 15% at 14+ days)
- Late-return fees (1.5x daily rate per extra day)
- Revenue report grouped by vehicle type
- Thread-safe booking operations

## Architecture
```
com.carrental
├── model        Vehicle, Customer, Booking, enums
├── pricing      PricingStrategy + Standard / WeekendSurcharge / LongTermDiscount
├── repository   Generic InMemoryRepository<T>
├── service      RentalService (all business rules)
├── exception    RentalException, NotFoundException, VehicleUnavailableException
└── Main         Console UI
```

### Design decisions
| Decision | Why |
|---|---|
| **Strategy + Decorator** for pricing | New pricing rules can be stacked without modifying existing code (Open/Closed Principle) |
| **`BigDecimal`** for money | `double` causes rounding errors in financial calculations |
| **Injected `Clock`** | Makes date-dependent logic deterministic and testable |
| **Half-open date ranges `[start, end)`** | Lets one rental end the same day the next begins |
| **Generic repository** | Service doesn't know about storage; swap in JDBC/JPA later |
| **`synchronized` booking** | Prevents two concurrent requests from double-booking a car |
| **Custom exception hierarchy** | Clear, specific error handling in the UI layer |

## Run it
Requires JDK 17+.
```bash
# Without Maven
mkdir out && javac -d out $(find src/main -name '*.java')
java -cp out com.carrental.Main

# With Maven (also runs the tests)
mvn test
mvn package && java -jar target/car-rental-system-1.0.0.jar
```

## Ideas to extend (great interview talking points)
1. Persist to PostgreSQL with JDBC, then Spring Data JPA
2. Expose a REST API with Spring Boot
3. Add a `Payment` module and an invoice generator
4. Replace the O(n) availability scan with an interval tree or per-vehicle sorted bookings
5. Add a concurrency test using `ExecutorService` to prove no double-booking

## Resume bullets (edit to match what you build)
- Designed and built a car rental system in Java 17 with layered architecture, using Strategy and Decorator patterns for composable pricing rules
- Implemented date-overlap booking validation and thread-safe operations to prevent double-booking
- Wrote JUnit 5 tests covering pricing, availability, cancellation and late-fee logic

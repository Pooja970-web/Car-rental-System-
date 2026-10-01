package com.carrental.pricing;

import com.carrental.model.Vehicle;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Strategy pattern: different ways to price a rental. endDate is exclusive. */
public interface PricingStrategy {
    BigDecimal calculate(Vehicle vehicle, LocalDate startDate, LocalDate endDate);
}

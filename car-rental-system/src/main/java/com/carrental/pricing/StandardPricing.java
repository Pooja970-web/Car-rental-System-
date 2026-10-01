package com.carrental.pricing;

import com.carrental.model.Vehicle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class StandardPricing implements PricingStrategy {
    @Override
    public BigDecimal calculate(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return vehicle.getDailyRate()
                .multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);
    }
}

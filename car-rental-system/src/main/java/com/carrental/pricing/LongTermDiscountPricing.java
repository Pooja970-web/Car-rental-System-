package com.carrental.pricing;

import com.carrental.model.Vehicle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Decorator: 10% off for 7+ days, 15% off for 14+ days. */
public class LongTermDiscountPricing implements PricingStrategy {
    private final PricingStrategy delegate;

    public LongTermDiscountPricing(PricingStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public BigDecimal calculate(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {
        BigDecimal base = delegate.calculate(vehicle, startDate, endDate);
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        BigDecimal discount = days >= 14 ? new BigDecimal("0.15")
                            : days >= 7  ? new BigDecimal("0.10")
                            : BigDecimal.ZERO;
        return base.subtract(base.multiply(discount)).setScale(2, RoundingMode.HALF_UP);
    }
}

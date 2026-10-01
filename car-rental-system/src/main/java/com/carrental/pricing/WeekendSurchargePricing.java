package com.carrental.pricing;

import com.carrental.model.Vehicle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;

/** Decorator: adds a 20% surcharge for every Saturday/Sunday night in the rental. */
public class WeekendSurchargePricing implements PricingStrategy {
    private static final BigDecimal SURCHARGE = new BigDecimal("0.20");
    private final PricingStrategy delegate;

    public WeekendSurchargePricing(PricingStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public BigDecimal calculate(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {
        BigDecimal base = delegate.calculate(vehicle, startDate, endDate);
        long weekendDays = startDate.datesUntil(endDate)
                .filter(d -> d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY)
                .count();
        BigDecimal extra = vehicle.getDailyRate()
                .multiply(SURCHARGE)
                .multiply(BigDecimal.valueOf(weekendDays));
        return base.add(extra).setScale(2, RoundingMode.HALF_UP);
    }
}

package com.vaishnavi.purchase_decision_api.service;

import com.vaishnavi.purchase_decision_api.dtos.SavingsPlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class SavingsPlanCalculator {


    /**
     * @return the plan, or {@code null} when there is nothing left over each month
     *         to save with — there is no honest timeline to give in that case.
     */
    public SavingsPlan calculate(BigDecimal price, BigDecimal effectiveDisposable) {

        if (effectiveDisposable == null || effectiveDisposable.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // how much more they need beyond this month's disposable
        BigDecimal shortfall = price.subtract(effectiveDisposable);

        // they can put their full monthly disposable toward saving
        BigDecimal monthlySavings = effectiveDisposable;

        // months needed = shortfall ÷ monthly disposable, rounded UP
        int monthsNeeded = shortfall
                .divide(monthlySavings, 0, RoundingMode.CEILING)
                .add(BigDecimal.ONE)
                .intValue();

        LocalDate targetDate = LocalDate.now().plusMonths(monthsNeeded);

        return new SavingsPlan(shortfall, monthlySavings, monthsNeeded, targetDate);
    }
}

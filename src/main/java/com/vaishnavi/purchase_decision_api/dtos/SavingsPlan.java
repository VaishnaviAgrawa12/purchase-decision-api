package com.vaishnavi.purchase_decision_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@AllArgsConstructor
public class SavingsPlan {

    private BigDecimal shortfall;        // how much more they need
    private BigDecimal monthlySavings;    // save this much per week
    private int monthsNeeded;             // for this many weeks
    private LocalDate targetDate;

}

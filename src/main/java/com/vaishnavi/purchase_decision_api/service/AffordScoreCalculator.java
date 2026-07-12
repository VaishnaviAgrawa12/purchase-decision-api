package com.vaishnavi.purchase_decision_api.service;

import com.vaishnavi.purchase_decision_api.enums.PurchaseType;
import com.vaishnavi.purchase_decision_api.enums.UsageFrequency;

import java.math.BigDecimal;

public class AffordScoreCalculator {

    public int calculateScore(BigDecimal monthlyIncome,
                              BigDecimal totalFixedExpenses,
                              BigDecimal savingsTarget,
                              BigDecimal monthlyRecurringCost,
                              BigDecimal price,
                              PurchaseType purchaseType,
                              UsageFrequency usageFrequency) {

        return 0; // placeholder
    }
}

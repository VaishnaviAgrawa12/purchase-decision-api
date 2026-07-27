package com.vaishnavi.purchase_decision_api.service;


import com.vaishnavi.purchase_decision_api.dtos.ScoreResult;
import com.vaishnavi.purchase_decision_api.enums.PurchaseType;
import com.vaishnavi.purchase_decision_api.enums.UsageFrequency;
import com.vaishnavi.purchase_decision_api.enums.Verdict;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

public class AffordScoreCalculatorTest {
    private final AffordScoreCalculator calculator = new AffordScoreCalculator();

    // helper so each test reads cleanly — profile is always the same
    private ScoreResult score(String price, PurchaseType type, UsageFrequency usage) {
        return calculator.calculateScore(
                new BigDecimal("62500"),   // income
                new BigDecimal("23000"),   // fixed
                new BigDecimal("12500"),   // savings → disposable = 27000
                BigDecimal.ZERO,           // recurring
                new BigDecimal(price),
                type, usage);
    }

    @Test
    void cheapItemShouldReturnBuy() {
        assertEquals(Verdict.BUY, score("3000", PurchaseType.WANT, UsageFrequency.MONTHLY).getVerdict());
    }

    @Test
    void priceExactlyAtDisposableIsWait() {
        // ratio exactly 1.00 → base 50 → WAIT (the boundary you designed)
        assertEquals(Verdict.WAIT, score("27000", PurchaseType.WANT, UsageFrequency.MONTHLY).getVerdict());
    }




}

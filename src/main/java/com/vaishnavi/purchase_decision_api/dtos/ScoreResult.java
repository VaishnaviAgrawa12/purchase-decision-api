package com.vaishnavi.purchase_decision_api.dtos;

import com.vaishnavi.purchase_decision_api.enums.Verdict;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ScoreResult {

    private int score;
    private Verdict verdict;
    private BigDecimal disposableIncome;
    private BigDecimal effectiveDisposable;
}

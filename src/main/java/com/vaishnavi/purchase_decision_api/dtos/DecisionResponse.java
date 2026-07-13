package com.vaishnavi.purchase_decision_api.dtos;


import com.vaishnavi.purchase_decision_api.enums.Verdict;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DecisionResponse {

    private String itemName;
    private BigDecimal price;
    private Verdict verdict;
    private int affordScore;
    private String aiExplanation;

    private BigDecimal disposableIncome;    // show them their breathing room
    private SavingsPlan savingsPlan;
}

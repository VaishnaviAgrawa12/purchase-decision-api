package com.vaishnavi.purchase_decision_api.dtos;

import com.vaishnavi.purchase_decision_api.enums.IncomeBracket;
import com.vaishnavi.purchase_decision_api.enums.InputMode;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FinancialProfileRequest {

    private IncomeBracket incomeBracket;

    // savings — amount or percentage
    private InputMode savingsInputMode;
    private BigDecimal savingsValue;

    // fixed expenses — EITHER a list of line items, OR one total percentage
    private InputMode fixedInputMode;
    private BigDecimal fixedPercentageValue;          // used if PERCENTAGE
    private List<FixedExpenseInput> fixedExpenses;    // used if AMOUNT

    @Data
    public static class FixedExpenseInput {
        private String name;
        private BigDecimal amount;
    }

}

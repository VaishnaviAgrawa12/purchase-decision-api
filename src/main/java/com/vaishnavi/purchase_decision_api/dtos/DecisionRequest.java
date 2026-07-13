package com.vaishnavi.purchase_decision_api.dtos;

import com.vaishnavi.purchase_decision_api.enums.PurchaseType;
import com.vaishnavi.purchase_decision_api.enums.UsageFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DecisionRequest {

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Item price is required")
    private BigDecimal price;

    @NotNull(message = "Purchase Type is required")
    private PurchaseType purchaseType;

    private String category;

    private String reason;

    private UsageFrequency usageFrequency;

    private BigDecimal monthlyRecurringCost;

}

package com.vaishnavi.purchase_decision_api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@AllArgsConstructor
@Schema(name = "SavingsPlan", description = "Returned only when the verdict is WAIT.")
public class SavingsPlan {

    @Schema(description = "How much more you need beyond this month's disposable income.",
            example = "6000.00")
    private BigDecimal shortfall;        // how much more they need

    @Schema(description = "Amount to set aside each month.", example = "24000.00")
    private BigDecimal monthlySavings;    // save this much per month

    @Schema(description = "Months of saving at that pace.", example = "2")
    private int monthsNeeded;             // for this many months

    @Schema(description = "The date you can buy it.", example = "2026-10-07", format = "date")
    private LocalDate targetDate;

}

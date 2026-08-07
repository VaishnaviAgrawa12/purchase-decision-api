package com.vaishnavi.purchase_decision_api.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vaishnavi.purchase_decision_api.enums.IncomeBracket;
import com.vaishnavi.purchase_decision_api.enums.InputMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(name = "FinancialProfileRequest",
        description = """
                Your monthly money picture. Savings and fixed expenses can each be given
                either as a rupee AMOUNT or as a PERCENTAGE of income — pick a mode per
                field and supply the matching value.""")
public class FinancialProfileRequest {

    @NotNull(message = "Income bracket is required")
    @Schema(description = "Income range. The engine scores against the bracket midpoint.",
            example = "FROM_50K_TO_75K", requiredMode = Schema.RequiredMode.REQUIRED)
    private IncomeBracket incomeBracket;

    // savings — amount or percentage
    @NotNull(message = "Savings input mode is required")
    @Schema(description = "Is `savingsValue` a rupee amount or a percentage of income?",
            example = "PERCENTAGE", requiredMode = Schema.RequiredMode.REQUIRED)
    private InputMode savingsInputMode;

    @NotNull(message = "Savings value is required")
    @PositiveOrZero(message = "Savings value cannot be negative")
    @Schema(description = "Monthly savings target — rupees if mode is AMOUNT, 0–100 if PERCENTAGE.",
            example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal savingsValue;

    // fixed expenses — EITHER a list of line items, OR one total percentage
    @NotNull(message = "Fixed expense input mode is required")
    @Schema(description = "PERCENTAGE — send `fixedPercentageValue`. AMOUNT — send `fixedExpenses`.",
            example = "AMOUNT", requiredMode = Schema.RequiredMode.REQUIRED)
    private InputMode fixedInputMode;

    @PositiveOrZero(message = "Fixed expense percentage cannot be negative")
    @Schema(description = "Total fixed expenses as a percentage (0–100) of income. "
            + "Required only when `fixedInputMode` is PERCENTAGE.", example = "30")
    private BigDecimal fixedPercentageValue;          // used if PERCENTAGE

    @Valid
    @Schema(description = "Itemised monthly fixed expenses. "
            + "Required only when `fixedInputMode` is AMOUNT.")
    private List<FixedExpenseInput> fixedExpenses;    // used if AMOUNT

    // The three checks below span more than one field, so they live on derived
    // getters rather than on a single property. The "check" prefix tells
    // GlobalExceptionHandler to report the message on its own, without a
    // meaningless field name in front of it.

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "savingsValue must be between 0 and 100 when savingsInputMode is PERCENTAGE")
    public boolean isCheckSavingsPercentage() {
        if (savingsInputMode != InputMode.PERCENTAGE || savingsValue == null) {
            return true;
        }
        return savingsValue.compareTo(BigDecimal.valueOf(100)) <= 0;
    }

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "fixedPercentageValue (0-100) is required when fixedInputMode is PERCENTAGE")
    public boolean isCheckFixedPercentage() {
        if (fixedInputMode != InputMode.PERCENTAGE) {
            return true;
        }
        return fixedPercentageValue != null
                && fixedPercentageValue.compareTo(BigDecimal.valueOf(100)) <= 0;
    }

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "fixedExpenses must contain at least one item when fixedInputMode is AMOUNT")
    public boolean isCheckFixedExpenseList() {
        if (fixedInputMode != InputMode.AMOUNT) {
            return true;
        }
        return fixedExpenses != null && !fixedExpenses.isEmpty();
    }

    @Data
    @Schema(name = "FixedExpenseInput", description = "One recurring monthly bill.")
    public static class FixedExpenseInput {

        @NotBlank(message = "Fixed expense name is required")
        @Schema(description = "What the bill is for.", example = "Rent",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @NotNull(message = "Fixed expense amount is required")
        @PositiveOrZero(message = "Fixed expense amount cannot be negative")
        @Schema(description = "Monthly amount in rupees.", example = "18000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private BigDecimal amount;
    }

}

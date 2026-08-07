package com.vaishnavi.purchase_decision_api.dtos;

import com.vaishnavi.purchase_decision_api.enums.PurchaseType;
import com.vaishnavi.purchase_decision_api.enums.UsageFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "DecisionRequest", description = "The purchase you're considering.")
public class DecisionRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 200, message = "Item name cannot exceed 200 characters")
    @Schema(description = "What you want to buy.", example = "Sony WH-1000XM5 headphones",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String itemName;

    @NotNull(message = "Item price is required")
    @Positive(message = "Price must be greater than zero")
    @Schema(description = "One-off price in rupees.", example = "30000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "Purchase Type is required")
    @Schema(description = "NEED is scored leniently, LUXURY strictly.", example = "WANT",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private PurchaseType purchaseType;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    @Schema(description = "Optional free-text category.", example = "Electronics")
    private String category;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    @Schema(description = "Optional note on why you want it.",
            example = "My current pair broke and I take calls all day")
    private String reason;

    @Schema(description = "Optional. How often you'd use it. Defaults to MONTHLY.",
            example = "DAILY", defaultValue = "MONTHLY")
    private UsageFrequency usageFrequency;

    @PositiveOrZero(message = "Monthly recurring cost cannot be negative")
    @Schema(description = "Optional. Ongoing monthly cost the purchase adds "
            + "(subscription, EMI, insurance). Defaults to 0.",
            example = "0", defaultValue = "0")
    private BigDecimal monthlyRecurringCost;

}

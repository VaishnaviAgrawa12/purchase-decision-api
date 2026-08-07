package com.vaishnavi.purchase_decision_api.dtos;


import com.vaishnavi.purchase_decision_api.enums.Verdict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Schema(name = "DecisionResponse", description = "The verdict, the score, and how to get there.")
public class DecisionResponse {

    @Schema(description = "Echoes the item you submitted.", example = "Sony WH-1000XM5 headphones")
    private String itemName;

    @Schema(description = "Echoes the price you submitted.", example = "30000")
    private BigDecimal price;

    @Schema(description = "BUY (score ≥ 70), WAIT (40–69) or SKIP (< 40).", example = "WAIT")
    private Verdict verdict;

    @Schema(description = "Affordability score, 0–100.", example = "65", minimum = "0", maximum = "100")
    private int affordScore;

    @Schema(description = "Plain-English reasoning. Falls back to a rule-based sentence "
            + "if the LLM is unavailable, so this is never empty.",
            example = "Those headphones are within reach, but not this month without "
                    + "squeezing your budget — holding off a little makes it painless.")
    private String aiExplanation;

    @Schema(description = "Monthly income minus fixed expenses minus your savings target.",
            example = "24000.00")
    private BigDecimal disposableIncome;    // show them their breathing room

    @Schema(description = "How to afford it. Present only when the verdict is WAIT, "
            + "otherwise null.", nullable = true)
    private SavingsPlan savingsPlan;
}

package com.vaishnavi.purchase_decision_api.dtos;

import com.vaishnavi.purchase_decision_api.entity.User;
import com.vaishnavi.purchase_decision_api.enums.IncomeBracket;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(name = "UserProfileResponse",
        description = "The saved financial profile, with the derived monthly figures the "
                + "scoring engine will use.")
public class UserProfileResponse {

    @Schema(description = "Your user id.", example = "3f1a7c22-5c4b-4c1e-9f60-2c2a1b9d8e77",
            format = "uuid")
    private UUID userId;

    @Schema(description = "Display name.", example = "Vaishnavi")
    private String name;

    @Schema(description = "Account email.", example = "tester@example.com")
    private String email;

    @Schema(description = "The bracket you selected.", example = "FROM_50K_TO_75K")
    private IncomeBracket incomeBracket;

    @Schema(description = "Bracket midpoint — the figure the engine actually scores against.",
            example = "62500")
    private BigDecimal monthlyIncome;

    @Schema(description = "Monthly savings target, resolved to rupees.", example = "12500.00")
    private BigDecimal savingsTarget;

    @Schema(description = "Your fixed expenses, resolved to rupees.")
    private List<FixedExpenseView> fixedExpenses;

    @Schema(description = "Sum of the active fixed expenses.", example = "18000.00")
    private BigDecimal totalFixedExpenses;

    @Schema(description = "monthlyIncome − totalFixedExpenses − savingsTarget. "
            + "This is what every decision is scored against.", example = "32000.00")
    private BigDecimal disposableIncome;

    public static UserProfileResponse from(User user) {
        BigDecimal totalFixed = user.getTotalFixedExpenses();
        return new UserProfileResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getIncomeBracket(),
                user.getMonthlyIncome(),
                user.getSavingsTarget(),
                user.getFixedExpenses().stream()
                        .map(fe -> new FixedExpenseView(fe.getExpenseName(), fe.getExpenseAmount()))
                        .toList(),
                totalFixed,
                user.getMonthlyIncome()
                        .subtract(totalFixed)
                        .subtract(user.getSavingsTarget())
        );
    }

    @Data
    @AllArgsConstructor
    @Schema(name = "FixedExpenseView", description = "One stored monthly fixed expense.")
    public static class FixedExpenseView {

        @Schema(description = "What the bill is for.", example = "Rent")
        private String name;

        @Schema(description = "Monthly amount in rupees.", example = "18000.00")
        private BigDecimal amount;
    }
}

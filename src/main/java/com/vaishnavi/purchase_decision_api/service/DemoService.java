package com.vaishnavi.purchase_decision_api.service;

import com.vaishnavi.purchase_decision_api.dtos.AuthResponse;
import com.vaishnavi.purchase_decision_api.dtos.FinancialProfileRequest;
import com.vaishnavi.purchase_decision_api.entity.User;
import com.vaishnavi.purchase_decision_api.enums.IncomeBracket;
import com.vaishnavi.purchase_decision_api.enums.InputMode;
import com.vaishnavi.purchase_decision_api.repository.UserRepository;
import com.vaishnavi.purchase_decision_api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Ready-made accounts so anyone handed the Swagger link can get a verdict without
 * inventing an email, a password and a financial profile first.
 *
 * <p>Two flavours:
 * <ul>
 *   <li>a <b>shared</b> account with fixed credentials, so the login example on the
 *       docs page works as-is;</li>
 *   <li>a <b>throwaway</b> account minted per request, so two people testing at the
 *       same time can't overwrite each other's profile.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class DemoService {

    public static final String SHARED_DEMO_EMAIL = "demo@purchasedecision.app";

    /** Same password for both flavours, so a throwaway account can be logged back into. */
    public static final String DEMO_PASSWORD = "Demo@1234";

    private static final String DEMO_NAME = "Demo User";

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Creates the shared demo account if it's missing, and resets its financial
     * profile either way — a previous visitor may have overwritten it.
     */
    @Transactional
    public void createOrResetSharedAccount() {
        User user = userRepository.findByEmail(SHARED_DEMO_EMAIL)
                .orElseGet(() -> newDemoUser(SHARED_DEMO_EMAIL));

        userService.setFinancialProfile(user, demoProfile());
    }

    /**
     * Mints a brand-new demo account with the profile already filled in and returns
     * a token for it. The caller can go straight to POST /api/decision.
     */
    @Transactional
    public AuthResponse createThrowawayAccount() {
        String email = "demo-" + UUID.randomUUID().toString().substring(0, 8)
                + "@purchasedecision.app";

        User user = userService.setFinancialProfile(newDemoUser(email), demoProfile());

        return new AuthResponse(user.getName(), user.getEmail(),
                jwtUtil.generateToken(user.getEmail()));
    }

    private User newDemoUser(String email) {
        User user = new User();
        user.setName(DEMO_NAME);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        return userRepository.save(user);
    }

    /**
     * Income 62,500 − fixed 25,500 − savings 12,500 = <b>24,500 disposable</b>.
     * Chosen so the sample purchases span all three verdicts: a cheap item is a
     * clear BUY, the 30,000 headphones land just over budget and return WAIT with a
     * savings plan, and anything large is a SKIP.
     */
    private FinancialProfileRequest demoProfile() {
        FinancialProfileRequest profile = new FinancialProfileRequest();
        profile.setIncomeBracket(IncomeBracket.FROM_50K_TO_75K);

        profile.setSavingsInputMode(InputMode.PERCENTAGE);
        profile.setSavingsValue(new BigDecimal("20"));

        profile.setFixedInputMode(InputMode.AMOUNT);
        profile.setFixedExpenses(List.of(
                fixedExpense("Rent", "18000"),
                fixedExpense("Groceries", "6000"),
                fixedExpense("Phone and internet", "1500")
        ));
        return profile;
    }

    private FinancialProfileRequest.FixedExpenseInput fixedExpense(String name, String amount) {
        FinancialProfileRequest.FixedExpenseInput input =
                new FinancialProfileRequest.FixedExpenseInput();
        input.setName(name);
        input.setAmount(new BigDecimal(amount));
        return input;
    }
}

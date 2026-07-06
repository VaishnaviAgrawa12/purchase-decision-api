package com.vaishnavi.purchase_decision_api.entity;

import com.vaishnavi.purchase_decision_api.enums.IncomeBracket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    // --- identity ---
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    // --- financial profile ---
    @Enumerated(EnumType.STRING)
    private IncomeBracket incomeBracket;   // what the user chose (for display)

    private BigDecimal monthlyIncome;      // the bracket midpoint (for calculation)

    private BigDecimal savingsTarget;

    // --- relationships ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedExpense> fixedExpenses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Decision> decisions = new ArrayList<>();

    // --- helper: sums only active fixed expenses, used by the score calculator ---
    public BigDecimal getTotalFixedExpenses() {
        return fixedExpenses.stream()
                .filter(FixedExpense::getActive)
                .map(FixedExpense::getExpenseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
package com.vaishnavi.purchase_decision_api.entity;

import com.vaishnavi.purchase_decision_api.enums.PrimaryMoneyGoal;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;

    private BigDecimal monthlyIncome;
    private BigDecimal savingsTarget;

    @Enumerated(EnumType.STRING)
    private PrimaryMoneyGoal primaryMoneyGoal;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedExpense> fixedExpenses = new ArrayList<>();

    // sums only active fixed expenses — used by the score calculator
    public BigDecimal getTotalFixedExpenses() {
        return fixedExpenses.stream()
                .filter(FixedExpense::getActive)
                .map(FixedExpense::getExpenseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Decision> decisions = new ArrayList<>();
}
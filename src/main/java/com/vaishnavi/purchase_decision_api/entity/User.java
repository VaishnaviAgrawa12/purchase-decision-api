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
    private BigDecimal savingTarget;

    @Enumerated(EnumType.STRING)
    private PrimaryMoneyGoal primaryMoneyGoal;
    //← EMERGENCY_FUND / SAVING_FOR_GOAL / MINDFUL_SPENDING

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FixedExpense> fixedExpenses = new ArrayList<>();;
    //← rent, EMIs, subscriptions, utilities


}

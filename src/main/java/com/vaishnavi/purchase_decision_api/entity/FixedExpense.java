package com.vaishnavi.purchase_decision_api.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fixed_expenses")
public class FixedExpense {

    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID expenseId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String expenseName;

    @Column(nullable = false)
    private BigDecimal expenseAmount;

    private Boolean active;



}

package com.vaishnavi.purchase_decision_api.repository;

import com.vaishnavi.purchase_decision_api.entity.FixedExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FixedExpenseRepository extends JpaRepository<FixedExpense, UUID> {
    List<FixedExpense> findByUserUserId(UUID userId);
}

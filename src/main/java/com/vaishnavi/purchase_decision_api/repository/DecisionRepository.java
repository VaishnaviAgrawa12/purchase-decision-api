package com.vaishnavi.purchase_decision_api.repository;

import com.vaishnavi.purchase_decision_api.entity.Decision;
import com.vaishnavi.purchase_decision_api.enums.Verdict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DecisionRepository extends JpaRepository <Decision, UUID>{

    List<Decision> findByUserUserId(UUID userId);
    List<Decision> findByUserUserIdAndVerdict(UUID userId, Verdict verdict);

}

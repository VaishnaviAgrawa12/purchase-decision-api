package com.vaishnavi.purchase_decision_api.service;

import com.vaishnavi.purchase_decision_api.dtos.DecisionRequest;
import com.vaishnavi.purchase_decision_api.dtos.DecisionResponse;
import com.vaishnavi.purchase_decision_api.dtos.SavingsPlan;
import com.vaishnavi.purchase_decision_api.dtos.ScoreResult;
import com.vaishnavi.purchase_decision_api.entity.Decision;
import com.vaishnavi.purchase_decision_api.entity.User;
import com.vaishnavi.purchase_decision_api.enums.UsageFrequency;
import com.vaishnavi.purchase_decision_api.enums.Verdict;
import com.vaishnavi.purchase_decision_api.repository.DecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DecisionService {

 private final DecisionRepository decisionRepository;
 private final LlmClient llmClient;

 private final AffordScoreCalculator scoreCalculator = new AffordScoreCalculator();
 private final SavingsPlanCalculator savingsPlanCalculator = new SavingsPlanCalculator();

 public DecisionResponse makeDecision(User user , DecisionRequest decisionRequest){

  if (user.getMonthlyIncome() == null) {
   throw new IllegalStateException("Please set your financial profile before making a decision");
  }

  UsageFrequency usage = (decisionRequest.getUsageFrequency() != null)
          ? decisionRequest.getUsageFrequency()
          : UsageFrequency.MONTHLY;

  BigDecimal recurringCost = (decisionRequest.getMonthlyRecurringCost() != null)
                           ? decisionRequest.getMonthlyRecurringCost()
                           : BigDecimal.ZERO;

  // score + verdict + disposable, all from one call
  ScoreResult result = scoreCalculator.calculateScore(
          user.getMonthlyIncome(),
          user.getTotalFixedExpenses(),
          user.getSavingsTarget(),
          recurringCost,
          decisionRequest.getPrice(),
          decisionRequest.getPurchaseType(),
          usage
  );

  // only compute a savings plan when they can't afford it now
  SavingsPlan savingsPlan = null;
  if (result.getVerdict() == Verdict.WAIT) {
   savingsPlan = savingsPlanCalculator.calculate(
           decisionRequest.getPrice(),
           result.getEffectiveDisposable()
   );
  }

   Decision decision = new Decision();
   decision.setUser(user);
   decision.setItemName(decisionRequest.getItemName());
   decision.setPrice(decisionRequest.getPrice());
   decision.setCategory(decisionRequest.getCategory());
   decision.setPurchaseType(decisionRequest.getPurchaseType());
   decision.setReason(decisionRequest.getReason());
   decision.setUsageFrequency(usage);
   decision.setMonthlyRecurringCost(recurringCost);
   decision.setVerdict(result.getVerdict());
   decision.setAffordScore(result.getScore());
  String explanation = llmClient.explainDecision(
          decisionRequest.getItemName(),
          decisionRequest.getPrice(),
          result.getVerdict(),
          result.getScore(),
          result.getDisposableIncome()
  );
  decision.setAiExplanation(explanation);

   decisionRepository.save(decision);


  return new DecisionResponse(
          decision.getItemName(),
          decision.getPrice(),
          result.getVerdict(),
          result.getScore(),
          decision.getAiExplanation(),
          result.getDisposableIncome(),
          savingsPlan
  );

 }

}

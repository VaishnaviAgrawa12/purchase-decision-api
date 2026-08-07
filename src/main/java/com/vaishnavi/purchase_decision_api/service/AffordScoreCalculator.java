package com.vaishnavi.purchase_decision_api.service;

import com.vaishnavi.purchase_decision_api.dtos.ScoreResult;
import com.vaishnavi.purchase_decision_api.enums.PurchaseType;
import com.vaishnavi.purchase_decision_api.enums.UsageFrequency;
import com.vaishnavi.purchase_decision_api.enums.Verdict;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AffordScoreCalculator {

    public ScoreResult calculateScore(BigDecimal monthlyIncome,
                                      BigDecimal totalFixedExpenses,
                                      BigDecimal savingsTarget,
                                      BigDecimal monthlyRecurringCost,
                                      BigDecimal price,
                                      PurchaseType purchaseType,
                                      UsageFrequency usageFrequency) {

        int score;

        BigDecimal disposable = monthlyIncome
                                .subtract(totalFixedExpenses)
                                .subtract(savingsTarget);
        BigDecimal effectiveDisposable = disposable.subtract(monthlyRecurringCost);

        // if there's no disposable income, nothing is affordable
        // NEED still gets a minimum score so it never SKIPs; everything else scores 0
        // returning here also keeps the ratio below from dividing by zero
        if (effectiveDisposable.compareTo(BigDecimal.ZERO) <= 0) {
            score = (purchaseType == PurchaseType.NEED) ? 40 : 0;
            Verdict floorVerdict = (score >= 40) ? Verdict.WAIT : Verdict.SKIP;
            return new ScoreResult(score, floorVerdict, disposable, effectiveDisposable);
        }

        BigDecimal ratio = price.divide(effectiveDisposable, 2, RoundingMode.HALF_UP);


        if (ratio.compareTo(new BigDecimal("0.20")) <= 0)      score = 90;
        else if (ratio.compareTo(new BigDecimal("0.40")) <= 0) score = 80;
        else if (ratio.compareTo(new BigDecimal("0.60")) <= 0) score = 72;
        else if (ratio.compareTo(new BigDecimal("0.85")) <= 0) score = 60;
        else if (ratio.compareTo(BigDecimal.ONE) <= 0)         score = 50;
        else if (ratio.compareTo(new BigDecimal("1.50")) <= 0) score = 45;
        else                                                    score = 15;

        if(purchaseType == PurchaseType.NEED){
            score += 15;
        }else if (purchaseType == PurchaseType.WANT){
            score += 0;
        }else{
            score -= 15;
        }

        if(usageFrequency == UsageFrequency.DAILY){
            score += 20;
        }else if (usageFrequency == UsageFrequency.WEEKLY){
            score += 10;
        }else if (usageFrequency == UsageFrequency.MONTHLY){
            score += 0;
        }else{
            score -= 10;
        }

        if (score > 100) {
            score = 100;
        } else if (score < 0) {
            score = 0;
        }

        if (ratio.compareTo(BigDecimal.ONE) > 0 && score > 69) {
            score = 69;   // over budget → WAIT at best, never BUY
        }

        Verdict verdict;

        if (score >= 70){
            verdict = Verdict.BUY;
        }else if (score >= 40){
            verdict = Verdict.WAIT;
        }else {
            verdict = Verdict.SKIP;
        }

        return new ScoreResult(score, verdict, disposable, effectiveDisposable);
    }
}

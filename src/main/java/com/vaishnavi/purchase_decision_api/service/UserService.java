package com.vaishnavi.purchase_decision_api.service;

import com.vaishnavi.purchase_decision_api.dtos.FinancialProfileRequest;
import com.vaishnavi.purchase_decision_api.entity.FixedExpense;
import com.vaishnavi.purchase_decision_api.entity.User;
import com.vaishnavi.purchase_decision_api.enums.InputMode;
import com.vaishnavi.purchase_decision_api.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User setFinancialProfile (User user, FinancialProfileRequest financialProfileRequest){


        user.setIncomeBracket(financialProfileRequest.getIncomeBracket());
        user.setMonthlyIncome(financialProfileRequest.getIncomeBracket().getMidpoint());

        BigDecimal income = financialProfileRequest.getIncomeBracket().getMidpoint();
        BigDecimal savings;
        if(financialProfileRequest.getSavingsInputMode() == InputMode.PERCENTAGE){
            savings = income
                    .multiply(financialProfileRequest.getSavingsValue())
                    .divide(BigDecimal.valueOf(100));
        }else{
            savings = financialProfileRequest.getSavingsValue();
        }
        user.setSavingsTarget(savings);

        user.getFixedExpenses().clear();



        if(financialProfileRequest.getFixedInputMode() == InputMode.PERCENTAGE){
            BigDecimal fixedExpense = income
                    .multiply(financialProfileRequest.getFixedPercentageValue())
                    .divide(BigDecimal.valueOf(100));
            user.getFixedExpenses().add(buildFixedExpense(user, "Total Fixed Expenses", fixedExpense));
        }else if (financialProfileRequest.getFixedExpenses() != null){
            for(FinancialProfileRequest.FixedExpenseInput input: financialProfileRequest.getFixedExpenses()){
                user.getFixedExpenses().add(buildFixedExpense(user, input.getName(), input.getAmount()));
            }

        }



        return userRepository.save(user);
    }

    private FixedExpense buildFixedExpense(User user, String name, BigDecimal amount){
        FixedExpense fe = new FixedExpense();
        fe.setUser(user);
        fe.setExpenseName(name);
        fe.setExpenseAmount(amount);
        fe.setActive(true);
        return fe;
    }

}

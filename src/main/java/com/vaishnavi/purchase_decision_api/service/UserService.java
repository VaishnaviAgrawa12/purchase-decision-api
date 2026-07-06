package com.vaishnavi.purchase_decision_api.service;

import com.vaishnavi.purchase_decision_api.dtos.FinancialProfileRequest;
import com.vaishnavi.purchase_decision_api.entity.User;
import com.vaishnavi.purchase_decision_api.repository.UserRepository;

public class UserService {
    private UserRepository userRepository;

    public User setFinancialProfile (User user, FinancialProfileRequest financialProfileRequest){


        user.setIncomeBracket(financialProfileRequest.getIncomeBracket());
        user.setMonthlyIncome(financialProfileRequest.getIncomeBracket().getMidpoint());
//        user.setSavingsTarget(financialProfileRequest.getSavingsValue());
//        user.setFixedExpenses(financialProfileRequest.getFixedExpenses());



        return userRepository.save(user);
    }

}

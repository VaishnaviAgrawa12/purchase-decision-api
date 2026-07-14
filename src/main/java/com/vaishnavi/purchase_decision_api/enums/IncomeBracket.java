package com.vaishnavi.purchase_decision_api.enums;

import java.math.BigDecimal;

public enum IncomeBracket {

    BELOW_25K(new BigDecimal("20000")),
    FROM_25K_TO_50K(new BigDecimal("37500")),
    FROM_50K_TO_75K(new BigDecimal("62500")),
    FROM_75K_TO_1L(new BigDecimal("87500")),
    FROM_1L_TO_1_5L(new BigDecimal("125000")),
    ABOVE_1_5L(new BigDecimal("175000"));

    private BigDecimal midpoint;

    IncomeBracket(BigDecimal midpoint){
        this.midpoint = midpoint;
    }

    public BigDecimal getMidpoint(){
        return midpoint;
    }
}

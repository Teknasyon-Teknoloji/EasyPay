package com.simpayment.utils;

/**
 * Created by okandroid on 12/07/16.
 */
public enum BillingType {

    IN_APP_BILLING("inapp"),
    SUBSCRIPTION("subs");

    private String type;

    BillingType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}

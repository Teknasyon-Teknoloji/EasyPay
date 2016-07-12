package com.simpayment.callbacks;

/**
 * Created by okandroid on 01/07/16.
 */
public interface BillingCompleteCallback {
    void onBillingComplete(String purchaseData, String billingType);
}

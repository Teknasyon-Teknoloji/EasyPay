package com.teknasyon.billingtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.simpayment.callbacks.BillingAutoRenewCallback;
import com.simpayment.callbacks.BillingCompleteCallback;
import com.simpayment.core.SimPayment;
import com.simpayment.utils.BillingType;

import java.util.ArrayList;

/**
 * READ ME!
 * In this simple billing project, you have to implement BillingAutoRenewCallback and
 * BillingCompleteCallback in your activity class. They overrides two method which one is onBillingComplete and
 * the other is onBillingAutoRenew. As you can guess, onBillingComplete method say to us that in first
 * parameter, the billing operation is finished, you can call your own web service as you wish. And
 * in the second parameter, you can understand easly type of billing. onBillingAutoRenew help us for
 * understanding whether the user's continuous subscribtion or not. If subscription is end or cont.
 * you can set something about user.
 * <p/>
 * Created by okandroid on 01/07/16.
 */
public class PaymentActivity extends AppCompatActivity implements BillingAutoRenewCallback, BillingCompleteCallback {
    Context context;
    //keystorepass: teknasyon2016
    //alias : inapp
    //keystorefile : billlingtest.jks
    SimPayment.PaymentBuilder paymentBuilder;
    TextView btnPremium;
    TextView btnCredit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        context = this;

        btnPremium = (TextView) findViewById(R.id.btnPr);
        btnCredit = (TextView) findViewById(R.id.btnGas);

        final ArrayList<String> productArray = new ArrayList<>();
        productArray.add("premium");
        productArray.add("credit");

        paymentBuilder = new SimPayment.PaymentBuilder(this);

        btnPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //subscription requirement
                paymentBuilder.developerPayload("developerPayload")
                        .products(productArray)
                        .paymentCompleteCallback(PaymentActivity.this) // need to add complete callback for handling when operation is finish.
                        .paymentRenewCallback(PaymentActivity.this) // need to add renew callback for subscription
                        .requestItem() // must to add request item before pay operation
                        .isSubscription(true) // if process is subscription, you have to set true this line
                        .item(productArray.get(0)) // you have to set your product id
                        .buildPayItem(); // starts to payment operation.
            }
        });

        btnCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //inapp requirement
                paymentBuilder.developerPayload("developerPayload")
                        .products(productArray)
                        .paymentCompleteCallback(PaymentActivity.this)
                        .requestItem()
                        .isSubscription(false)
                        .item(productArray.get(1))
                        .buildPayItem();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // after clicked buy button on google payment dialog, you must handle action with this line.
        paymentBuilder.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBillingAutoRenew(String purchaseData) {

    }

    /**
     * Returns purchase data and billing type when billing operation finish.
     *
     * @param purchaseData
     * @param billingType
     */
    @Override
    public void onBillingComplete(String purchaseData, String billingType) {

        // There is two type of billing. One of is subscription and the other is inapp. You can access
        // that in BillingTypes class and distinguish billing type with using BillingType enums
        if (billingType.equals(BillingType.IN_APP_BILLING.getType()))
            Toast.makeText(PaymentActivity.this, "CREDID HAS BEEN CHARGE!" + purchaseData, Toast.LENGTH_SHORT).show();
        if (billingType.equals(BillingType.SUBSCRIPTION.getType()))
            Toast.makeText(PaymentActivity.this, "YOU ARE PREMIUM MEMBER RIGHT NOW!" + purchaseData, Toast.LENGTH_SHORT).show();

        /*
        Simple purchaseData output:
        {
         "orderId":"GPA.1234-5678-9012-34567",
         "packageName":"com.example.app",
         "productId":"exampleSku",
         "purchaseTime":1345678900000,
         "purchaseState":0,
         "developerPayload":"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ",
         "purchaseToken":"opaque-token-up-to-1000-characters"
         }
         */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        paymentBuilder.unBindPaymentService();
    }
}

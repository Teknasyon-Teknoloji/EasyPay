package com.simpayment.core;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;

import com.google.gson.Gson;
import com.simpayment.InAppBillingService;
import com.simpayment.callbacks.BillingAutoRenewCallback;
import com.simpayment.callbacks.BillingCompleteCallback;
import com.simpayment.model.BillingPurchaseResponseModel;
import com.simpayment.utils.BillingType;
import com.simpayment.utils.Billingargs;
import com.simpayment.utils.GsonFactory;

import java.util.ArrayList;

/**
 * Created by okandroid on 03/07/16.
 */
public class SimPayment {

    private final ArrayList<String> products;
    private final Activity activity;
    private final String developerPayload;
    private final boolean isSubscription;

    private SimPayment(PaymentBuilder builder) {
        this.activity = builder.activity;
        this.developerPayload = builder.developerPayload;
        this.isSubscription = builder.isSubscription;
        this.products = builder.products;
    }

    public ArrayList<String> getProducts() {
        return products;
    }

    public Activity getActivity() {
        return activity;
    }

    public String getDeveloperPayload() {
        return developerPayload;
    }

    public boolean isSubscription() {
        return isSubscription;
    }

    public static class PaymentBuilder {
        static final int RC_REQUEST = 1001;
        private ArrayList<String> products;
        private Activity activity;
        private String developerPayload;
        private String item;
        private boolean isSubscription;
        Context context;
        BillingCompleteCallback billingCompleteCallback;
        BillingAutoRenewCallback billingAutoRenewCallback;

        Gson factory;
        InAppBillingService inAppBillingService;

        public PaymentBuilder(Activity context) {
            this.activity = context;
            factory = GsonFactory.getInstance();
            inAppBillingService = new InAppBillingService(context, true);
        }

        public PaymentBuilder paymentCompleteCallback(BillingCompleteCallback callback) {
            this.billingCompleteCallback = callback;
            return this;
        }

        public PaymentBuilder paymentRenewCallback(BillingAutoRenewCallback callback) {
            this.billingAutoRenewCallback = callback;
            return this;
        }

        public PaymentBuilder products(ArrayList<String> products) {
            this.products = products;
            return this;
        }

        public PaymentBuilder activity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public PaymentBuilder item(String item) {
            this.item = item;
            return this;
        }

        /**
         * With the In-app Billing Version 3 API, you can include a 'developer payload' string token
         * when sending your purchase request to Google Play. Typically, this is used to pass in a
         * string token that uniquely identifies this purchase request. If you specify a string value,
         * Google Play returns this string along with the purchase response. Subsequently, when you
         * make queries about this purchase, Google Play returns this string together with the
         * purchase details.
         * You should pass in a string token that helps your application to identify the user who
         * made the purchase, so that you can later verify that this is a legitimate purchase by that
         * user. For consumable items, you can use a randomly generated string, but for non- consumable
         * items you should use a string that uniquely identifies the user.
         *
         * @param developerPayload
         * @return
         */
        public PaymentBuilder developerPayload(String developerPayload) {
            this.developerPayload = developerPayload;
            return this;
        }

        /**
         * When you are buying something, you have to specify its type. Renewable or what ?
         * Only set boolen.
         *
         * @param isSubscription
         * @return
         */
        public PaymentBuilder isSubscription(boolean isSubscription) {
            this.isSubscription = isSubscription;
            return this;
        }

        /**
         * Querying for Items Available for Purchase.
         * In your application, you can query the item details from Google Play using the
         * In-app Billing Version 3 API. To pass a request to the In-app Billing service, first
         * create a Bundle that contains a String ArrayList of product IDs with key "ITEM_ID_LIST",
         * where each string is a product ID for an purchasable item.
         *
         * @return
         */
        public PaymentBuilder requestItem() {
            SimPayment payment = new SimPayment(this);
            requestItem(payment.products);
            return this;
        }

        /**
         * Starts buyProduct action. Let's have a money!
         *
         * @return
         */
        public SimPayment buildPayItem() {
            SimPayment payment = new SimPayment(this);
            buyProduct(item);
            return payment;
        }

        public void unBindPaymentService() {
            activity.unbindService(inAppBillingService.getmBillingServiceConnection());
        }

        /**
         * To pass a request to the In-app Billing service, first create a Bundle that contains a
         * String ArrayList of product IDs with key "ITEM_ID_LIST", where each string is a product ID
         * for an purchasable item.
         *
         * @param data
         */
        public void requestItem(final ArrayList<String> data) {
            AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Bundle>() {

                @Override
                protected Bundle doInBackground(Void... params) {
                    ArrayList<String> skuList = new ArrayList<>();

                    for (int i = 0; i < data.size(); i++) {
                        skuList.add(data.get(i));
                    }
                    Bundle querySkus = new Bundle();
                    querySkus.putStringArrayList(Billingargs.ITEM_ID_LIST, skuList);
                    Bundle skuDetails = null;
                    try {
                        if (inAppBillingService.getmBillingService() != null) {
                            String billingType = isSubscription ? BillingType.SUBSCRIPTION.getType()
                                    : BillingType.IN_APP_BILLING.getType();
                            skuDetails = inAppBillingService.getmBillingService().getSkuDetails(3,
                                    activity.getPackageName(), billingType, querySkus);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return skuDetails;
                }

                @Override
                protected void onPostExecute(Bundle skuDetails) {
                    super.onPostExecute(skuDetails);
                    if (skuDetails == null) {
                        return;
                    }
                }
            });
        }

        /**
         * To start a purchase request from your app, call the getBuyIntent method on the
         * In-app Billing service. Pass in to the method the In-app Billing API version (“3”),
         * the package name of your calling app, the product ID for the item to purchase, the purchase
         * type (“inapp” or "subs"), and a developerPayload String. The developerPayload String is
         * used to specify any additional arguments that you want Google Play to send back along with
         * the purchase information.
         *
         * @param DATA
         */
        public void buyProduct(final String DATA) {
            AsyncTaskCompat.executeParallel(new AsyncTask<String, Void, Bundle>() {
                @Override
                protected Bundle doInBackground(String... params) {
                    String payLoad = developerPayload;

                    Bundle buyIntentBundle = null;
                    try {
                        String type = isSubscription ? BillingType.SUBSCRIPTION.getType() :
                                BillingType.IN_APP_BILLING.getType();
                        buyIntentBundle = inAppBillingService.getmBillingService()
                                .getBuyIntent(3, activity.getPackageName(), DATA, type, payLoad);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return buyIntentBundle;
                }

                @Override
                protected void onPostExecute(Bundle buyIntentBundle) {
                    super.onPostExecute(buyIntentBundle);

                    try {
                        PendingIntent pendingIntent = buyIntentBundle.getParcelable(Billingargs.BUY_INTENT);
                        //GOOGLE PAYMENT DIALOG WILL OPEN
                        activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
                                RC_REQUEST, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                Integer.valueOf(0));
                    } catch (Exception e) {
                        //payment failed
                    }
                }
            });
        }

        /**
         * How you use the consumption mechanism in your app is up to you. Typically, you would
         * implement consumption for in-app products with temporary benefits that users may want to
         * purchase multiple times (for example, in-game currency or equipment). You would typically
         * not want to implement consumption for in-app products that are purchased once and provide
         * a permanent effect (for example, a premium upgrade).
         * <p/>
         * To record a purchase consumption, send the consumePurchase method to the In-app Billing
         * service and pass in the purchaseToken String value that identifies the purchase to be
         * removed. The purchaseToken is part of the data returned in the INAPP_PURCHASE_DATA String
         * by the Google Play service following a successful purchase request. In this example, you are
         * recording the consumption of a product that is identified with the purchaseToken in the token
         * variable.
         *
         * @param purchaseToken
         */
        private void consumeProduct(final String purchaseToken) {
            AsyncTaskCompat.executeParallel(new AsyncTask<String, Void, Integer>() {
                @Override
                protected Integer doInBackground(String... params) {
                    int response = -1;
                    try {
                        if (inAppBillingService.getmBillingService() != null) {
                            response = inAppBillingService.getmBillingService()
                                    .consumePurchase(3, activity.getPackageName(), purchaseToken);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return response;
                }

                @Override
                protected void onPostExecute(Integer response) {
                    super.onPostExecute(response);
                }
            });
        }

        /**
         * Google Play sends a response to your PendingIntent to the onActivityResult method of your
         * application. The onActivityResult method will have a result code of Activity.RESULT_OK (1)
         * or Activity.RESULT_CANCELED (0). To see the types of order information that is returned
         * in the response Intent, see In-app Billing Reference.
         *
         * @param requestCode
         * @param resultCode
         * @param data
         */
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case RC_REQUEST:
                    if (data != null) {
                        String purchaseData = data.getStringExtra(Billingargs.INAPP_PURCHASE_DATA);
                        if (purchaseData == null)
                            return;
                        BillingPurchaseResponseModel model = factory.fromJson(purchaseData, BillingPurchaseResponseModel.class);
                        consumeProduct(model.purchaseToken);
                        billingCompleteCallback.onBillingComplete(purchaseData, isSubscription ?
                                BillingType.SUBSCRIPTION.getType() : BillingType.IN_APP_BILLING.getType());

                    }
                    break;
                default:
                    break;
            }
        }

    }

}

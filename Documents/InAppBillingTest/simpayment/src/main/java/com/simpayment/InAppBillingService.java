package com.simpayment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.simpayment.callbacks.BillingAutoRenewCallback;
import com.simpayment.callbacks.IBillingConnection;
import com.simpayment.model.BillingPurchaseResponseModel;
import com.simpayment.utils.BillingType;
import com.simpayment.utils.Billingargs;
import com.simpayment.utils.GsonFactory;

import java.util.ArrayList;

/**
 * Created by okandroid on 29/06/16.
 */
public class InAppBillingService implements IBillingConnection {

    public static final String TAG = InAppBillingService.class.getName();
    IInAppBillingService mBillingService;
    Context context;
    boolean willBeRequested;
    BillingAutoRenewCallback billingAutoRenewCallback;
    Gson factory;

    /**
     *
     */
    public InAppBillingService(Context context,
                               boolean willBeRequested) {
        this.context = context;
        this.initCallback = this;
        this.willBeRequested = willBeRequested;
        factory = GsonFactory.getInstance();
        doBindBillingService();
    }

    /**
     * OPEN BILLING SERVICE CONNECTION
     */
    protected IBillingConnection initCallback;
    ServiceConnection mBillingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBillingService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBillingService = IInAppBillingService.Stub.asInterface(service);
            initCallback.onCompleted(true);
        }
    };

    @Override
    public void onCompleted(boolean isCompleted) {
        if (willBeRequested)
            getRenewPurchaseInfo();
    }

    /**
     * CHECKING GOOGLE PAYMENTS WEB SERVICE.
     */
    private void getRenewPurchaseInfo() {
        try {
            Bundle b = mBillingService.getPurchases(3, context.getPackageName(), BillingType.SUBSCRIPTION.getType(), null);
            int response = b.getInt(Billingargs.RESPONSE_CODE);
            if (response == 0) {
                Log.d(TAG, "SUBSCRIPTION getRenewPurchaseInfo: " + "NEW TAX FOUND");
                ArrayList<String> purchaseDataList = b.getStringArrayList(Billingargs.INAPP_PURCHASE_DATA_LIST);
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    BillingPurchaseResponseModel model = factory.fromJson(purchaseData, BillingPurchaseResponseModel.class);
                    if (model.autoRenewing) {
                        //user has new bill info. send billing to web service.
                        billingAutoRenewCallback.onBillingAutoRenew(purchaseData);
                        Log.d(TAG, "SUBSCRIPTION getRenewPurchaseInfo: ORDER DATA: " + purchaseData);
                    }
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    void doBindBillingService() {
        Intent serviceIntent = new Intent(Billingargs.vendingPackage.concat(".billing.InAppBillingService.BIND"));
        serviceIntent.setPackage(Billingargs.vendingPackage);
        context.bindService(serviceIntent, mBillingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public ServiceConnection getmBillingServiceConnection() {
        return mBillingServiceConnection;
    }

    public IInAppBillingService getmBillingService() {
        return mBillingService;
    }

}

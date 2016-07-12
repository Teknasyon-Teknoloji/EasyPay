package com.simpayment.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by okandroid on 29/06/16.
 */
public class BillingPurchaseResponseModel implements Serializable {

    @SerializedName("packageName")
    public String packageName;

    @SerializedName("productId")
    public String productId;

    @SerializedName("purchaseTime")
    public Long purchaseTime;

    @SerializedName("purchaseState")
    public int purchaseState;

    @SerializedName("developerPayload")
    public String developerPayload;

    @SerializedName("purchaseToken")
    public String purchaseToken;

    @SerializedName("autoRenewing")
    public boolean autoRenewing;

    @Override
    public String toString() {
        return "BillingPurchaseResponseModel{" +
                "packageName='" + packageName + '\'' +
                ", productId='" + productId + '\'' +
                ", purchaseTime=" + purchaseTime +
                ", purchaseState=" + purchaseState +
                ", developerPayload='" + developerPayload + '\'' +
                ", purchaseToken='" + purchaseToken + '\'' +
                ", autoRenewing=" + autoRenewing +
                '}';
    }
}

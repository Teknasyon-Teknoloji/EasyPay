package com.simpayment.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by okandroid on 29/06/16.
 */
public class GsonFactory {
    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("dd.MM.yyyy HH:mm:ss");
        gson = gsonBuilder.create();
    }

    public static Gson getInstance() {
        return gson;
    }
}

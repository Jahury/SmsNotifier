package com.apptech.smsdispatcher.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * Created by Jahury on 3/13/2018.
 */

public class GsonUtils {
    private static String TAG="coredroid";

    private static Gson gson;
    /**
     * Add specific parsing to gson
     *
     * @return new instance of {@link Gson}
     */
    public static Gson getGsonParser() {

        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Filters.class, new DeserializerFilters());
            gson = gsonBuilder.create();
        }
        return gson;
    }

    }

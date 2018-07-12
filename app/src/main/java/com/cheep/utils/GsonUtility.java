package com.cheep.utils;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kruti on 6/3/18.
 */

public class GsonUtility {

    /**
     * GSON UTILITY Methods
     */
    private static Gson gson = new GsonBuilder().create();

    public static Object getObjectFromJsonString(String jsonData, Class modelClass) {
        return gson.fromJson(jsonData, modelClass);
    }

    public static String getJsonStringFromObject(Object modelClass) {
        return gson.toJson(modelClass);
    }

    public static <T> String getJsonStringFromObject(List<T> objectArrayList) {
        return gson.toJson(objectArrayList, new TypeToken<List<T>>() {
        }.getType());
    }

    @NonNull
    public static <T> ArrayList<T> getObjectListFromJsonString(String jsonData, Class myclass) {
        return new ArrayList<>(Arrays.asList((T[]) gson.fromJson(jsonData, myclass)));
    }
}

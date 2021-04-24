package com.chen.mumucarapp.control;

import org.apache.commons.lang.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;

public class Session {
    private static Map<String,String> sharedPreferences;

    public Session() {
        this.sharedPreferences = new HashMap();
    }

    public void clear() {
        this.sharedPreferences.clear();
    }

    public int getInt(String paramString, int paramInt) {
        return NumberUtils.toInt(this.sharedPreferences.get(paramString), paramInt);
    }


    public boolean saveInt(String paramString, int paramInt) {
        this.sharedPreferences.put(paramString, paramInt+"");
        return true;
    }
}

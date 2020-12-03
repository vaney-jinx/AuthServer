package com.jinx.authserver.constant;

public class Constant {
    public static String getAuthParamKey(String clientId){
        return "auth-param:" + clientId;
    }

    public static String getAuthCodeKey(String clientId) {
        return "auth-code:" + clientId;
    }
}

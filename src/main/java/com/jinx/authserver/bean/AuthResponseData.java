package com.jinx.authserver.bean;

public class AuthResponseData {
    private String accessToken;
    private String refreshToken;
    private Long validityTime;

    private AuthResponseData() {
    }

    private AuthResponseData(String accessToken, String refreshToken, Long validityTime) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.validityTime = validityTime;
    }

    public static AuthResponseData build(String accessToken, String refreshToken, Long validityTime) {
        return new AuthResponseData(accessToken, refreshToken, validityTime);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getValidityTime() {
        return validityTime;
    }

    public void setValidityTime(Long validityTime) {
        this.validityTime = validityTime;
    }
}

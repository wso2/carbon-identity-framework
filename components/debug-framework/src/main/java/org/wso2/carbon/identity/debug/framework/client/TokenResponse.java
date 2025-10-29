package org.wso2.carbon.identity.debug.framework.client;

/**
 * Simple holder for OAuth2 token response values.
 */
public class TokenResponse {

    private final String accessToken;
    private final String idToken;
    private final String refreshToken;
    private final String tokenType;

    public TokenResponse(String accessToken, String idToken, String refreshToken, String tokenType) {
        this.accessToken = accessToken;
        this.idToken = idToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}

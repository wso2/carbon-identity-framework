package org.wso2.carbon.identity.application.authentication.framework.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class is model class of a federated token.
 * A federated token is an external token obtained via an OIDC federated authenticator
 * after a successful authentication.
 */
public class FederatedToken implements Serializable {

    private static final long serialVersionUID = 6618332057931299623L;
    private String idp;
    @JsonProperty("expires_in")
    private String tokenValidityPeriod;
    private String scope;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;

    // Constructor
    public FederatedToken(String idp, String accessToken) {

        this.idp = idp;
        this.accessToken = accessToken;
    }

    // Getters and setters
    public String getIdp() {

        return idp;
    }

    public void setIdp(String idp) {

        this.idp = idp;
    }

    public String getTokenValidityPeriod() {

        return tokenValidityPeriod;
    }

    public void setTokenValidityPeriod(String tokenValidityPeriod) {

        this.tokenValidityPeriod = tokenValidityPeriod;
    }

    public String getScope() {

        return scope;
    }

    public void setScope(String scope) {

        this.scope = scope;
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

    @Override
    public String toString() {

        return "FederatedToken{" + "idp='" + idp + '\'' + ", tokenValidityPeriod='" + tokenValidityPeriod + '\'' +
                ", scope='" + scope + '\'' + ", accessToken='" + accessToken + '\'' + ", refreshToken='" +
                refreshToken + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FederatedToken that = (FederatedToken) o;
        return idp.equals(that.idp) && tokenValidityPeriod.equals(that.tokenValidityPeriod) &&
                scope.equals(that.scope) && accessToken.equals(that.accessToken) &&
                Objects.equals(refreshToken, that.refreshToken);
    }

    @Override
    public int hashCode() {

        return Objects.hash(idp, tokenValidityPeriod, scope, accessToken, refreshToken);
    }
}

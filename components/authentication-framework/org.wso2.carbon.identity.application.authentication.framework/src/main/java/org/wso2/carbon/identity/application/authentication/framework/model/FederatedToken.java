/*
 *
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 LLC. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 *
 */

package org.wso2.carbon.identity.application.authentication.framework.model;

import java.io.Serializable;

/**
 * This class is model class of a federated token.
 * A federated token is an external token obtained via an OIDC federated authenticator
 * after a successful authentication.
 */
public class FederatedToken implements Serializable {

    private static final long serialVersionUID = 6618332057931299623L;
    private String idp;
    private String tokenValidityPeriod;
    private String scope;
    private String accessToken;
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
}

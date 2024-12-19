/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    // Space delimited list of the scopes bounded to the federated token.
    private String scope;
    private String accessToken;
    private String refreshToken;

    /**
     * A federated token should be created with the identity provider and the access token.
     *
     * @param idp         The federated authenticator name which returned the access token.
     * @param accessToken The access token.
     */
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

    /**
     * Get the space delimited list of the scopes bounded to the federated token.
     *
     * @return Space delimited list of the scopes bounded to the federated token.
     */
    public String getScope() {

        return scope;
    }

    /**
     * Set the space delimited list of the scopes bounded to the federated token.
     *
     * @param scope Space delimited list of the scopes bounded to the federated token.
     */
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

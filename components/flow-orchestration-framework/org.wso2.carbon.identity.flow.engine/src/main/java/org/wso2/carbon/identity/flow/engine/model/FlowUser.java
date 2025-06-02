/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.engine.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.flow.engine.Constants.USERNAME_CLAIM_URI;

/**
 * This class is responsible for holding the user profile of the registering user.
 */
public class FlowUser implements Serializable {

    private static final long serialVersionUID = -1873658743998134877L;
    private final Map<String, String> claims = new HashMap<>();
    private final Map<String, char[]> userCredentials = new HashMap<>();
    private String username;

    public String getUsername() {

        if (username == null) {
            username = claims.get(USERNAME_CLAIM_URI);
        }
        return username;
    }

    public void setUsername(String username) {

        this.username = username;
        this.claims.put(USERNAME_CLAIM_URI, username);
    }

    public Map<String, String> getClaims() {

        return claims;
    }

    public void addClaims(Map<String, String> claims) {

        this.claims.putAll(claims);
    }

    public Object getClaim(String claimUri) {

        return this.claims.get(claimUri);
    }

    public void addClaim(String claimUri, String claimValue) {

        this.claims.put(claimUri, claimValue);
    }

    public Map<String, char[]> getUserCredentials() {

        return userCredentials;
    }

    public void setUserCredentials(Map<String, char[]> credentials) {

        this.userCredentials.putAll(credentials);
    }
}

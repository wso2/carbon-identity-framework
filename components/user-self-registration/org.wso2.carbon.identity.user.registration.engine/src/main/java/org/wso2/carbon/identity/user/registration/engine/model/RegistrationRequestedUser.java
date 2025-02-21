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

package org.wso2.carbon.identity.user.registration.engine.model;

import org.wso2.carbon.identity.user.registration.engine.temp.RegistrationConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RegistrationRequestedUser implements Serializable {

    private static final long serialVersionUID = -1873658743998134877L;

    private String username;
    private final Map<String, Object> claims = new HashMap<>();
    private final Map<String, String> userCredentials = new HashMap<>();

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
        // Update the username claim as well.
        this.claims.put(RegistrationConstants.USERNAME_CLAIM_URI, username);
    }

    public Map<String, Object> getClaims() {

        return claims;
    }

    public void addClaims(Map<String, Object> claims) {

        this.claims.putAll(claims);
    }

    public Object getClaim(String claimUri) {

        return this.claims.get(claimUri);
    }

    public void addClaim(String claimUri, String claimValue) {

        this.claims.put(claimUri, claimValue);
    }

    public Map<String, String> getUserCredentials() {

        return userCredentials;
    }

    public void addUserCredentials(Map<String, String> credentials) {

        this.userCredentials.putAll(credentials);
    }

    public void addUserCredential(String key, String value) {

        this.userCredentials.put(key, value);
    }
}

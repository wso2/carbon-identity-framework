/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the data related to an authenticator during an authentication flow.
 * Contains data specific to authentication flow.
 */
public class AuthenticatorData {

    private String name;
    private String displayName;
    private String idp;
    private List<AuthenticatorParamMetadata> authParams = new ArrayList<>();
    private Map<String, String> additionalData = new HashMap<>();

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public String getIdp() {

        return idp;
    }

    public void setIdp(String idp) {

        this.idp = idp;
    }

    public List<AuthenticatorParamMetadata> getAuthParams() {

        return authParams;
    }

    public void setAuthParams(List<AuthenticatorParamMetadata> authParams) {

        this.authParams = authParams;
    }

    public Map<String, String> getAdditionalData() {

        return additionalData;
    }

    public void setAdditionalData(Map<String, String> additionalData) {

        this.additionalData = additionalData;
    }
}

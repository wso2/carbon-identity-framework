/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the data related to an authenticator during an authentication flow.
 * Contains data specific to authentication flow.
 */
public class AuthenticatorData {

    private String name;
    private String displayName;
    private String idp;
    private String i18nKey;
    private List<AuthenticatorParamMetadata> authParams = new ArrayList<>();
    private List<String> requiredParams = new ArrayList<>();
    private FrameworkConstants.AuthenticatorPromptType promptType;
    private AdditionalData additionalData;
    private AuthenticatorMessage authenticatorMessage;

    public String getI18nKey() {

        return i18nKey;
    }

    public void setI18nKey(String i18nKey) {

        this.i18nKey = i18nKey;
    }

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

    public List<String> getRequiredParams() {

        return requiredParams;
    }

    public void setRequiredParams(List<String> requiredParams) {

        this.requiredParams = requiredParams;
    }

    public FrameworkConstants.AuthenticatorPromptType getPromptType() {

        return promptType;
    }

    public void setPromptType(FrameworkConstants.AuthenticatorPromptType promptType) {

        this.promptType = promptType;
    }

    public AdditionalData getAdditionalData() {

        return additionalData;
    }

    public void setAdditionalData(AdditionalData additionalData) {

        this.additionalData = additionalData;
    }

    public AuthenticatorMessage getMessage() {

        return authenticatorMessage;
    }

    public void setMessage(AuthenticatorMessage authenticatorMessage) {

        this.authenticatorMessage = authenticatorMessage;
    }
}

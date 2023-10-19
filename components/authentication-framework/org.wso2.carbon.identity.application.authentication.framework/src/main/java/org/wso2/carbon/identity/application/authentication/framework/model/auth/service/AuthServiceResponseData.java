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

package org.wso2.carbon.identity.application.authentication.framework.model.auth.service;

import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatorData;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing authentication data that is used to communicate intermediate authentication steps.
 */
public class AuthServiceResponseData {

    private boolean isAuthenticatorSelectionRequired = false;
    private List<AuthenticatorData> authenticatorOptions = new ArrayList<>();

    public AuthServiceResponseData() {

    }

    public AuthServiceResponseData(List<AuthenticatorData> authenticatorOptions) {

        this.authenticatorOptions = authenticatorOptions;
    }

    public boolean isAuthenticatorSelectionRequired() {

        return isAuthenticatorSelectionRequired;
    }

    public void setAuthenticatorSelectionRequired(boolean authenticatorSelectionRequired) {

        isAuthenticatorSelectionRequired = authenticatorSelectionRequired;
    }

    public List<AuthenticatorData> getAuthenticatorOptions() {

        return authenticatorOptions;
    }

    public void setAuthenticatorOptions(List<AuthenticatorData> authenticatorOptions) {

        this.authenticatorOptions = authenticatorOptions;
    }
}

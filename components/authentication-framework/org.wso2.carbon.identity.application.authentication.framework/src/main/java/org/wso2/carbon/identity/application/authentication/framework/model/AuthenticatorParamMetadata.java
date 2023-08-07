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

/**
 * Contains the metadata of an auth related parameter used in an authenticator.
 */
public class AuthenticatorParamMetadata {

    private String name;
    private FrameworkConstants.AuthenticatorParamType type;
    private boolean isConfidential = false;
    private int paramOrder;
    private int paramGroup = 0;

    public AuthenticatorParamMetadata(String name, FrameworkConstants.AuthenticatorParamType type, int paramOrder) {

        this.name = name;
        this.type = type;
        this.paramOrder = paramOrder;
    }

    public AuthenticatorParamMetadata(String name, FrameworkConstants.AuthenticatorParamType type, int paramOrder,
                                      boolean isConfidential) {

        this.name = name;
        this.type = type;
        this.paramOrder = paramOrder;
        this.isConfidential = isConfidential;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public FrameworkConstants.AuthenticatorParamType getType() {

        return type;
    }

    public void setType(FrameworkConstants.AuthenticatorParamType type) {

        this.type = type;
    }

    public boolean isConfidential() {

        return isConfidential;
    }

    public void setConfidential(boolean confidential) {

        isConfidential = confidential;
    }

    public int getParamOrder() {

        return paramOrder;
    }

    public void setParamOrder(int paramOrder) {

        this.paramOrder = paramOrder;
    }

    public int getParamGroup() {

        return paramGroup;
    }

    public void setParamGroup(int paramGroup) {

        this.paramGroup = paramGroup;
    }
}

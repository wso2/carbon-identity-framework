/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import java.util.Map;

public class FrameworkLoginResponse extends IdentityResponse {

    private String authName;
    private String authType;
    private String contextKey;
    private String relyingParty;
    private String callbackPath;
    private String redirectUrl;
    private Map<String, String[]> parameterMap;

    public String getAuthName() {
        return authName;
    }

    public String getAuthType() {
        return authType;
    }

    public String getContextKey() {
        return contextKey;
    }

    public String getRelyingParty() {
        return relyingParty;
    }

    public String getCallbackPath() {
        return callbackPath;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    protected FrameworkLoginResponse(FrameworkLoginResponseBuilder builder) {
        super(builder);
        this.authName = builder.authName;
        this.authType = builder.authType;
        this.contextKey = builder.contextKey;
        this.relyingParty = builder.relyingParty;
        this.callbackPath = builder.callbackPath;
        this.redirectUrl = builder.redirectUrl;
        this.parameterMap = builder.parameterMap;
    }

    public static class FrameworkLoginResponseBuilder extends IdentityResponseBuilder {

        private String authName;
        private String authType;
        private String contextKey;
        private String relyingParty;
        private String callbackPath;
        private String redirectUrl;
        private Map<String, String[]> parameterMap;

        public FrameworkLoginResponseBuilder(IdentityMessageContext context) {
            super(context);
        }

        public FrameworkLoginResponseBuilder setAuthName(String authName) {
            this.authName = authName;
            return this;
        }

        public FrameworkLoginResponseBuilder setAuthType(String authType) {
            this.authType = authType;
            return this;
        }

        public FrameworkLoginResponseBuilder setContextKey(String contextKey) {
            this.contextKey = contextKey;
            return this;
        }

        public FrameworkLoginResponseBuilder setRelyingParty(String relyingParty) {
            this.relyingParty = relyingParty;
            return this;
        }

        public FrameworkLoginResponseBuilder setCallbackPath(String callbackPath) {
            this.callbackPath = callbackPath;
            return this;
        }

        public FrameworkLoginResponseBuilder setRedirectURL(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        public FrameworkLoginResponseBuilder setParameterMap(Map<String, String[]> parameterMap) {
            this.parameterMap = parameterMap;
            return this;
        }

        public FrameworkLoginResponse build() {
            return new FrameworkLoginResponse(this);
        }
    }
}

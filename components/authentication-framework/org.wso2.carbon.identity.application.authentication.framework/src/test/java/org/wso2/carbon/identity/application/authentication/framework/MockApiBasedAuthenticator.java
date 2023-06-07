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

package org.wso2.carbon.identity.application.authentication.framework;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatorData;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Mock API based authenticator.
 */
public class MockApiBasedAuthenticator extends AbstractApplicationAuthenticator {

    private String name;
    private AuthenticatorData authenticatorData;
    private boolean isAPIBasedAuthenticationSupported = true;

    public MockApiBasedAuthenticator(String name) {

        this.name = name;
    }

    public MockApiBasedAuthenticator(String name, String idp) {

        this.name = name;
        this.authenticatorData = new AuthenticatorData();
        this.authenticatorData.setName(name);
        this.authenticatorData.setDisplayName(getFriendlyName());
        this.authenticatorData.setIdp(idp);
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {

    }

    @Override
    public boolean canHandle(HttpServletRequest request) {

        return false;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {

        return null;
    }

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public String getFriendlyName() {

        return this.name + "-friendlyName";
    }

    @Override
    public boolean isAPIBasedAuthenticationSupported() {

        return isAPIBasedAuthenticationSupported;
    }

    @Override
    public Optional<AuthenticatorData> getAuthInitiationData(AuthenticationContext context) {

        return Optional.ofNullable(this.authenticatorData);
    }

    public void setAPIBasedAuthenticationSupported(boolean isAPIBasedAuthenticationSupported) {

        this.isAPIBasedAuthenticationSupported = isAPIBasedAuthenticationSupported;
    }
}

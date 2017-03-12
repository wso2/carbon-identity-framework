/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.authentication;

import org.wso2.carbon.identity.gateway.api.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.common.model.idp.AuthenticationConfig;
import org.wso2.carbon.identity.gateway.common.model.idp.AuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.idp.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractSequence extends AbstractGatewayHandler implements Serializable {

    private static final long serialVersionUID = 855941807514456712L;
    protected transient AuthenticationContext authenticationContext = null;

    protected AbstractSequence(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    protected AbstractSequence() {

    }

    public abstract IdentityProvider getIdentityProvider(int step, String identityProviderName)
            throws AuthenticationHandlerException;
    public abstract List<IdentityProvider> getIdentityProviders(int step)
            throws AuthenticationHandlerException;

    public abstract List<RequestPathAuthenticatorConfig> getRequestPathAuthenticatorConfig();

    public abstract int getSteps() throws AuthenticationHandlerException;

    public abstract boolean hasNext(int currentStep) throws AuthenticationHandlerException;

    public abstract boolean isMultiOption(int step) throws AuthenticationHandlerException;

    public abstract boolean isRequestPathAuthenticatorsAvailable();

    public abstract boolean isStepAuthenticatorAvailable() throws AuthenticationHandlerException;

    public abstract AuthenticationStepConfig getAuthenticationStepConfig(int step)   ;

    public abstract AuthenticatorConfig getAuthenticatorConfig(int step, String authenticatorName, String
            identityProvider)
            ;

}

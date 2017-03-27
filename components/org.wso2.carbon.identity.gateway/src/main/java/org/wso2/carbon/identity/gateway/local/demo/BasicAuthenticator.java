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
 *
 */

package org.wso2.carbon.identity.gateway.local.demo;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.authentication.authenticator.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.response.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.authentication.sequence.Sequence;
import org.wso2.carbon.identity.gateway.common.model.idp.AuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;
import org.wso2.carbon.identity.gateway.local.LocalAuthenticationRequest;
import org.wso2.carbon.identity.gateway.local.LocalAuthenticationResponse;
import org.wso2.carbon.identity.gateway.model.LocalUser;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.User;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.AuthenticationFailure;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;

import javax.security.auth.callback.PasswordCallback;
import java.util.List;

/**
 * This is dummy BasicAuthenticator to cover the local authentication. This should be move to the some other repo.
 */
public class BasicAuthenticator {

    // extends AbstractApplicationAuthenticator implements LocalApplicationAuthenticator
  /*  public static final String IS_RETRY_ENABLE = "isRetryEnable";
    private Logger log = LoggerFactory.getLogger(BasicAuthenticator.class);


    @Override
    public String getFriendlyName() {
        return "BasicAuthenticator";
    }

    @Override
    public String getName() {
        return "BasicAuthenticator";
    }


    @Override
    protected AuthenticationResponse processRequest(AuthenticationContext context)
            throws AuthenticationHandlerException {

        GatewayRequest identityRequest = context.getIdentityRequest();
        if (identityRequest instanceof LocalAuthenticationRequest) {
            LocalAuthenticationRequest localAuthenticationRequest = (LocalAuthenticationRequest) identityRequest;
            SequenceContext sequenceContext = context.getSequenceContext();
            SequenceContext.StepContext currentStepContext = sequenceContext.getCurrentStepContext();

            RealmService realmService = GatewayServiceHolder.getInstance().getRealmService();
            IdentityStore identityStore = realmService.getIdentityStore();
            Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/username",
                    localAuthenticationRequest.getUserName());
            PasswordCallback passwordCallback = new PasswordCallback("psasword", false);
            passwordCallback.setPassword(localAuthenticationRequest.getPassword().toCharArray());

            org.wso2.carbon.identity.mgt.AuthenticationContext context1 = null;
            try {
                context1 = identityStore.authenticate(claim, new
                        PasswordCallback[]{passwordCallback}, "PRIMARY");
            } catch (IdentityStoreException e) {
                String error = "Error occurred while authetnicating the user," + e.getMessage();
                log.error(error, e);
                throw new GatewayRuntimeException(error, e);
            } catch (AuthenticationFailure authenticationFailure) {
                throw new AuthenticationHandlerException("Authentication Failed.");
            }

            User user = context1.getUser();
            LocalUser localUser = new LocalUser(user);
            currentStepContext.setUser(localUser);
            return new AuthenticationResponse(AuthenticationResponse.Status.AUTHENTICATED);
        }
        LocalAuthenticationResponse.LocalAuthenticationResponseBuilder
                localAuthenticationResponseBuilder = new LocalAuthenticationResponse
                .LocalAuthenticationResponseBuilder();
        localAuthenticationResponseBuilder.setRelayState(context
                .getInitialAuthenticationRequest()
                .getRequestKey());
        localAuthenticationResponseBuilder.setEndpointURL("https://localhost:9292/gateway/endpoint");
        List<IdentityProvider> identityProviders = context.getSequence()
                .getIdentityProviders(context.getSequenceContext().getCurrentStep());
        StringBuilder idpList = new StringBuilder();
        identityProviders.forEach(identityProvider -> idpList.append(identityProvider
                .getAuthenticatorName() +
                ":" + identityProvider
                .getIdentityProviderName()
                + ","));
        localAuthenticationResponseBuilder.setIdentityProviderList(idpList.toString());
        AuthenticationResponse authenticationResponse = new AuthenticationResponse(localAuthenticationResponseBuilder);
        return authenticationResponse;
    }

    @Override
    protected AuthenticationResponse processResponse(AuthenticationContext context)
            throws AuthenticationHandlerException {

        GatewayRequest identityRequest = context.getIdentityRequest();
        if (identityRequest instanceof LocalAuthenticationRequest) {
            LocalAuthenticationRequest localAuthenticationRequest = (LocalAuthenticationRequest) identityRequest;
            try {
                SequenceContext sequenceContext = context.getSequenceContext();
                SequenceContext.StepContext currentStepContext = sequenceContext.getCurrentStepContext();

                RealmService realmService = GatewayServiceHolder.getInstance().getRealmService();
                IdentityStore identityStore = realmService.getIdentityStore();
                Claim claim = new Claim("http://wso2.org/claims", "http://wso2.org/claims/username",
                        localAuthenticationRequest.getUserName());
                PasswordCallback passwordCallback = new PasswordCallback("psasword", false);
                passwordCallback.setPassword(localAuthenticationRequest.getPassword().toCharArray());

                org.wso2.carbon.identity.mgt.AuthenticationContext context1 = identityStore.authenticate(claim, new
                        PasswordCallback[]{passwordCallback}, "PRIMARY");
                if (context1.isAuthenticated()) {
                    User user = context1.getUser();
                    LocalUser localUser = new LocalUser(user);
                    currentStepContext.setUser(localUser);
                    return new AuthenticationResponse(AuthenticationResponse.Status.AUTHENTICATED);
                }
                throw new AuthenticationHandlerException("Authentication Failed.");
            } catch (IdentityStoreException e) {
                String error = "Error occurred while authetnicating the user," + e.getMessage();
                log.error(error, e);
                throw new GatewayRuntimeException(error, e);
            } catch (AuthenticationFailure authenticationFailure) {
                throw new AuthenticationHandlerException("Authentication Failed.", authenticationFailure);
            }
        }
        throw new GatewayRuntimeException("Identity Request should be a LocalAuthenticationRequest.");
    }

    @Override
    public boolean isRetryEnable(AuthenticationContext context) {
        Sequence sequence = context.getSequence();
        SequenceContext.StepContext currentStepContext = context.getSequenceContext().getCurrentStepContext();
        AuthenticatorConfig authenticatorConfig = sequence.getAuthenticatorConfig(currentStepContext.getStep(), currentStepContext
                        .getAuthenticatorName(),
                currentStepContext.getIdentityProviderName());
        String retryEnableString = (String) authenticatorConfig.getProperties().get(IS_RETRY_ENABLE);
        boolean isRetryEnable = BooleanUtils.toBoolean(retryEnableString);
        return isRetryEnable;
    }*/
}

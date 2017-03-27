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
package org.wso2.carbon.identity.gateway.context;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.authentication.sequence.Sequence;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.context.cache.SessionContextCache;
import org.wso2.carbon.identity.gateway.exception.InvalidServiceProviderIdException;
import org.wso2.carbon.identity.gateway.exception.ServiceProviderIdNotSetException;
import org.wso2.carbon.identity.gateway.model.User;
import org.wso2.carbon.identity.gateway.request.AuthenticationRequest;
import org.wso2.carbon.identity.gateway.request.ClientAuthenticationRequest;
import org.wso2.carbon.identity.gateway.service.GatewayClaimResolverService;
import org.wso2.carbon.identity.gateway.store.ServiceProviderConfigStore;
import org.wso2.carbon.identity.mgt.claim.Claim;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * AuthenticationContext is the context that is shared through whole process of request.
 * <p>
 * For the initial request, this context will create and cache. Initial request also stored in this context as
 * ClientAuthenticationRequest.
 */
public class AuthenticationContext extends GatewayMessageContext {

    private static final long serialVersionUID = 6821167819709907062L;

    protected ClientAuthenticationRequest initialAuthenticationRequest;
    protected String serviceProviderId;

    private Sequence sequence = null;
    private SequenceContext sequenceContext = new SequenceContext();

    public AuthenticationContext(ClientAuthenticationRequest authenticationRequest, Map<Serializable, Serializable> parameters) {
        super(authenticationRequest, parameters);
        this.initialAuthenticationRequest = authenticationRequest;
    }

    public AuthenticationContext(ClientAuthenticationRequest authenticationRequest) {
        super(authenticationRequest);
        this.initialAuthenticationRequest = authenticationRequest;
    }

    /**
     * This is the initial request to the gateway and it MUST be a ClientAuthenticationRequest. Since we cache the
     * context until the request is authenticated successfully, any time we can get this initial request for any
     * handlers.
     *
     * @return ClientAuthenticationRequest
     */
    public ClientAuthenticationRequest getInitialAuthenticationRequest() {
        return initialAuthenticationRequest;
    }

    /**
     * This return the sequence that is build by the SequenceBuilder.
     *
     * @return Sequence
     */
    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(
            Sequence sequence) {
        this.sequence = sequence;
    }

    /**
     * Return SequenceContext.
     *
     * @return SequenceContext
     */
    public SequenceContext getSequenceContext() {
        return sequenceContext;
    }

    public void setSequenceContext(
            SequenceContext sequenceContext) {
        this.sequenceContext = sequenceContext;
    }

    public ServiceProviderConfig getServiceProvider() {
        if (StringUtils.isBlank(serviceProviderId)) {
            throw new ServiceProviderIdNotSetException("ServiceProviderId has not been set.");
        }
        ServiceProviderConfig serviceProvider = ServiceProviderConfigStore.getInstance().getServiceProvider(serviceProviderId);
        return serviceProvider;
    }

    public SessionContext getSessionContext() {
        GatewayRequest identityRequest = getIdentityRequest();
        if (identityRequest instanceof AuthenticationRequest) {
            AuthenticationRequest authenticationRequest = (AuthenticationRequest) identityRequest;
            String sessionKey = authenticationRequest.getSessionKey();
            if (StringUtils.isNotBlank(sessionKey)) {
                return SessionContextCache.getInstance().get(DigestUtils.sha256Hex(sessionKey));
            }
        }
        return null;
    }

    public String getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(String serviceProviderId) throws GatewayClientException {
        this.serviceProviderId = serviceProviderId;
        ServiceProviderConfig spConfig = getServiceProvider();
        if(spConfig == null) {
            this.serviceProviderId = null;
            throw new InvalidServiceProviderIdException("Invalid serviceProviderId: " + serviceProviderId);
        }
    }

    public User getSubjectUser() throws GatewayServerException {

        User subject = null;
        int lastStep = sequenceContext.getCurrentStep();
        boolean isUserIdStepFound = false;
        for (int i = 1; i < lastStep + 1; i++) {
            boolean isSubjectStep = false;
            AuthenticationStepConfig stepConfig = getSequence().getAuthenticationStepConfig(i);
            isSubjectStep = true; // update isSubjectStep using stepConfig
            if (isSubjectStep && isUserIdStepFound) {
                throw new GatewayServerException("Invalid subject step configuration. Multiple subject steps found.");
            } else {
                isUserIdStepFound = true;
                SequenceContext.StepContext stepContext = sequenceContext.getStepContext(i);
                subject = stepContext.getUser();
            }
        }
        return subject;
    }

    public Claim getSubjectClaim() {

        Set<Claim> claims = sequenceContext.getClaims();
        String subjectClaimUri = getServiceProvider().getClaimConfig().getSubjectClaimUri();
        for (Claim claim : claims) {
            if(claim.getClaimUri().equals(subjectClaimUri)) {
                return claim;
            }
        }
        return null;
    }

    public Set<Claim> getAttributes(AuthenticationContext context) {

        Set<Claim> aggregatedClaims = context.getSequenceContext().getClaims();
        String dialect = context.getServiceProvider().getClaimConfig().getDialectUri();
        String profileName = context.getServiceProvider().getClaimConfig().getProfile();

        aggregatedClaims = GatewayClaimResolverService.getInstance().transformToOtherDialect(
                aggregatedClaims, dialect, Optional.ofNullable(profileName));

        return aggregatedClaims;
    }

}

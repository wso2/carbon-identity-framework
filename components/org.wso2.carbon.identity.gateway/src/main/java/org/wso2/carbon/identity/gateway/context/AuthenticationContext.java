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
import org.wso2.carbon.identity.gateway.authentication.AbstractSequence;
import org.wso2.carbon.identity.gateway.cache.SessionContextCache;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.request.AuthenticationRequest;
import org.wso2.carbon.identity.gateway.request.ClientAuthenticationRequest;
import org.wso2.carbon.identity.gateway.store.ServiceProviderConfigStore;

import java.io.Serializable;
import java.util.Map;

public class AuthenticationContext<T1 extends Serializable, T2 extends Serializable>
        extends

        GatewayMessageContext<T1, T2, AuthenticationRequest> {

    private static final long serialVersionUID = 6821167819709907062L;

    protected ClientAuthenticationRequest initialAuthenticationRequest;
    protected String uniqueId;

    private AbstractSequence sequence = null;
    private SequenceContext sequenceContext = new SequenceContext();

    public AuthenticationContext(AuthenticationRequest authenticationRequest, Map<T1, T2> parameters) {
        super(authenticationRequest, parameters);
        if(authenticationRequest instanceof ClientAuthenticationRequest) {
            this.initialAuthenticationRequest = (ClientAuthenticationRequest) authenticationRequest;
        }
    }

    public AuthenticationContext(AuthenticationRequest authenticationRequest) {
        super(authenticationRequest);
        if(authenticationRequest instanceof ClientAuthenticationRequest) {
            this.initialAuthenticationRequest = (ClientAuthenticationRequest) authenticationRequest;
        }
    }

    public ClientAuthenticationRequest getInitialAuthenticationRequest() {
        return initialAuthenticationRequest;
    }

    public AbstractSequence getSequence() {
        return sequence;
    }

    public void setSequence(
            AbstractSequence sequence) {
        this.sequence = sequence;
    }

    public SequenceContext getSequenceContext() {
        return sequenceContext;
    }

    public void setSequenceContext(
            SequenceContext sequenceContext) {
        this.sequenceContext = sequenceContext;
    }

    public ServiceProviderConfig getServiceProvider() {
        String uniqueId = getUniqueId();
        ServiceProviderConfig serviceProvider = ServiceProviderConfigStore.getInstance().getServiceProvider(uniqueId);
        return serviceProvider;
    }

    public SessionContext getSessionContext() {
        AuthenticationRequest authenticationRequest = getIdentityRequest();
        String sessionKey = authenticationRequest.getSessionKey();
        if (StringUtils.isNotBlank(sessionKey)) {
            return SessionContextCache.getInstance().get(DigestUtils.sha256Hex(sessionKey));
        }
        return null;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

}

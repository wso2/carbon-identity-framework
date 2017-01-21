/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.gateway.api.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.api.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.gateway.api.IdentityProcessor;
import org.wso2.carbon.identity.gateway.processor.authenticator.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.ContextInitializer;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.RequestPathHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.SequenceManager;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.StepHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authorization.AbstractAuthorizationHandler;
import org.wso2.carbon.identity.gateway.processor.handler.claim.ClaimHandler;
import org.wso2.carbon.identity.gateway.processor.handler.extension.AbstractPostHandler;
import org.wso2.carbon.identity.gateway.processor.handler.extension.AbstractPreHandler;
import org.wso2.carbon.identity.gateway.processor.handler.extension.ExtensionHandlerPoints;
import org.wso2.carbon.identity.gateway.processor.handler.jit.JITHandler;
import org.wso2.carbon.identity.gateway.processor.handler.request.AbstractRequestHandler;
import org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameworkServiceDataHolder {
    private static FrameworkServiceDataHolder instance = new FrameworkServiceDataHolder();
    //Authenticators
    List<RequestPathApplicationAuthenticator> requestPathApplicationAuthenticators = new ArrayList<>();
    List<LocalApplicationAuthenticator> localApplicationAuthenticators = new ArrayList<>();
    List<FederatedApplicationAuthenticator> federatedApplicationAuthenticators = new ArrayList<>();
    //SequenceBuilder
    List<AbstractSequenceBuildFactory> sequenceBuildFactories = new ArrayList<>();
    List<SequenceManager> sequenceManagers = new ArrayList<>();
    List<StepHandler> stepHandlers = new ArrayList<>();
    List<RequestPathHandler> requestPathHandlers = new ArrayList<>();




    private BundleContext bundleContext = null;
    private long nanoTimeReference = 0;
    private long unixTimeReference = 0;
    private List<IdentityProcessor> identityProcessors = new ArrayList<IdentityProcessor>();
    private List<HttpIdentityRequestFactory> httpIdentityRequestFactories = new ArrayList<HttpIdentityRequestFactory>();
    private List<HttpIdentityResponseFactory> httpIdentityResponseFactories = new ArrayList<>();
    //Framework handlers
    private List<AbstractRequestHandler> requestHandlers = new ArrayList<>();
    private List<AuthenticationHandler> authenticationHandlers = new ArrayList<>();
    private List<AbstractAuthorizationHandler> authorizationHandlers = new ArrayList<>();
    private List<JITHandler> jitHandlers = new ArrayList<>();
    private List<ClaimHandler> claimHandlers = new ArrayList<>();

    //SequenceManager
    private List<AbstractResponseHandler> responseHandlers = new ArrayList<>();
    private Map<ExtensionHandlerPoints, List<AbstractPreHandler>> preHandler = new HashMap<>();
    private Map<ExtensionHandlerPoints, List<AbstractPostHandler>> postHandler = new HashMap<>();
    //AuthenticationHandler sub-handler
    private List<ContextInitializer> contextInitializers = new ArrayList<>();


    private FrameworkServiceDataHolder() {
        setNanoTimeReference(System.nanoTime());
        setUnixTimeReference(System.currentTimeMillis());
    }

    public static FrameworkServiceDataHolder getInstance() {
        return instance;
    }


    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public long getNanoTimeReference() {
        return nanoTimeReference;
    }

    private void setNanoTimeReference(long nanoTimeReference) {
        this.nanoTimeReference = nanoTimeReference;
    }

    public long getUnixTimeReference() {
        return unixTimeReference;
    }

    private void setUnixTimeReference(long unixTimeReference) {
        this.unixTimeReference = unixTimeReference;
    }

    public List<HttpIdentityRequestFactory> getHttpIdentityRequestFactories() {
        return httpIdentityRequestFactories;
    }

    public List<IdentityProcessor> getIdentityProcessors() {
        return identityProcessors;
    }

    public List<HttpIdentityResponseFactory> getHttpIdentityResponseFactories() {
        return httpIdentityResponseFactories;
    }

    public List<AuthenticationHandler> getAuthenticationHandlers() {
        return authenticationHandlers;
    }

    public List<AbstractAuthorizationHandler> getAuthorizationHandlers() {
        return authorizationHandlers;
    }

    public List<AbstractRequestHandler> getRequestHandlers() {
        return requestHandlers;
    }

    public List<JITHandler> getJitHandlers() {
        return jitHandlers;
    }

    public List<ClaimHandler> getClaimHandlers() {
        return claimHandlers;
    }

    public List<AbstractResponseHandler> getResponseHandlers() {
        return responseHandlers;
    }

    public Map<ExtensionHandlerPoints, List<AbstractPreHandler>> getPreHandler() {
        return preHandler;
    }

    public Map<ExtensionHandlerPoints, List<AbstractPostHandler>> getPostHandler() {
        return postHandler;
    }

    public List<ContextInitializer> getContextInitializers() {
        return contextInitializers;
    }

    public List<RequestPathApplicationAuthenticator> getRequestPathApplicationAuthenticators() {
        return requestPathApplicationAuthenticators;
    }

    public List<LocalApplicationAuthenticator> getLocalApplicationAuthenticators() {
        return localApplicationAuthenticators;
    }

    public List<FederatedApplicationAuthenticator> getFederatedApplicationAuthenticators() {
        return federatedApplicationAuthenticators;
    }

    public List<AbstractSequenceBuildFactory> getSequenceBuildFactories() {
        return sequenceBuildFactories;
    }


    public List<SequenceManager> getSequenceManagers() {
        return sequenceManagers;
    }

    public List<StepHandler> getStepHandlers() {
        return stepHandlers;
    }

    public List<RequestPathHandler> getRequestPathHandlers() {
        return requestPathHandlers;
    }
}

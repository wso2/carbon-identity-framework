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

package org.wso2.carbon.identity.gateway.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.claim.service.ClaimResolvingService;
import org.wso2.carbon.identity.claim.service.ProfileMgtService;
import org.wso2.carbon.identity.gateway.api.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;
import org.wso2.carbon.identity.gateway.processor.authenticator.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.ContextInitializer;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.RequestPathHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.SequenceManager;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.StepHandler;
import org.wso2.carbon.identity.gateway.processor.handler.request.AbstractRequestValidator;
import org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler;

import java.util.ArrayList;
import java.util.List;

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
    private List<GatewayProcessor> gatewayProcessors = new ArrayList<GatewayProcessor>();
    private List<GatewayRequestBuilderFactory> httpIdentityRequestFactories = new ArrayList<GatewayRequestBuilderFactory>();
    private List<GatewayResponseBuilderFactory> httpIdentityResponseFactories = new ArrayList<>();
    //Framework handlers
    private List<AbstractRequestValidator> requestHandlers = new ArrayList<>();
    private List<AuthenticationHandler> authenticationHandlers = new ArrayList<>();

    //SequenceManager
    private List<AbstractResponseHandler> responseHandlers = new ArrayList<>();
    //AuthenticationHandler sub-handler
    private List<ContextInitializer> contextInitializers = new ArrayList<>();

    private ClaimResolvingService claimResolvingService = null ;
    private ProfileMgtService profileMgtService = null ;



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

    public List<GatewayRequestBuilderFactory> getHttpIdentityRequestFactories() {
        return httpIdentityRequestFactories;
    }

    public List<GatewayProcessor> getGatewayProcessors() {
        return gatewayProcessors;
    }

    public List<GatewayResponseBuilderFactory> getHttpIdentityResponseFactories() {
        return httpIdentityResponseFactories;
    }

    public List<AuthenticationHandler> getAuthenticationHandlers() {
        return authenticationHandlers;
    }

    public List<AbstractRequestValidator> getRequestHandlers() {
        return requestHandlers;
    }

    public List<AbstractResponseHandler> getResponseHandlers() {
        return responseHandlers;
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

    public ClaimResolvingService getClaimResolvingService() {
        return claimResolvingService;
    }

    public void setClaimResolvingService(ClaimResolvingService claimResolvingService) {
        this.claimResolvingService = claimResolvingService;
    }

    public ProfileMgtService getProfileMgtService() {
        return profileMgtService;
    }

    public void setProfileMgtService(ProfileMgtService profileMgtService) {
        this.profileMgtService = profileMgtService;
    }

}

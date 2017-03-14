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
import org.wso2.carbon.identity.gateway.authentication.authenticator.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.authenticator.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.executer.AbstractExecutionHandler;
import org.wso2.carbon.identity.gateway.authentication.sequence.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.authentication.step.AuthenticationStepHandler;
import org.wso2.carbon.identity.gateway.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.gateway.handler.response.AbstractResponseHandler;
import org.wso2.carbon.identity.gateway.handler.session.AbstractSessionHandler;
import org.wso2.carbon.identity.gateway.handler.validator.AbstractRequestValidator;
import org.wso2.carbon.identity.mgt.RealmService;

import java.util.ArrayList;
import java.util.List;

/**
 * GatewayServiceHolder is the Service Holder Point.
 */
public class GatewayServiceHolder {

    private static GatewayServiceHolder instance = new GatewayServiceHolder();


    List<LocalApplicationAuthenticator> localApplicationAuthenticators = new ArrayList<>();
    List<FederatedApplicationAuthenticator> federatedApplicationAuthenticators = new ArrayList<>();
    //SequenceBuilder
    List<AbstractSequenceBuildFactory> sequenceBuildFactories = new ArrayList<>();
    List<AuthenticationStepHandler> authenticationStepHandlers = new ArrayList<>();
    List<AbstractExecutionHandler> executionHandlers = new ArrayList<>();
    //Authenticators
    private List<RequestPathApplicationAuthenticator> requestPathApplicationAuthenticators = new ArrayList<>();
    private BundleContext bundleContext = null;
    private long nanoTimeReference = 0;
    private long unixTimeReference = 0;
    private List<GatewayProcessor> gatewayProcessors = new ArrayList<GatewayProcessor>();
    private List<GatewayRequestBuilderFactory> httpIdentityRequestFactories
            = new ArrayList<GatewayRequestBuilderFactory>();
    private List<GatewayResponseBuilderFactory> httpIdentityResponseFactories = new ArrayList<>();
    //Framework handlers
    private List<AbstractRequestValidator> requestHandlers = new ArrayList<>();
    private List<AuthenticationHandler> authenticationHandlers = new ArrayList<>();
    private List<AbstractResponseHandler> responseHandlers = new ArrayList<>();
    private List<AbstractSessionHandler> sessionHandlers = new ArrayList<>();

    private ClaimResolvingService claimResolvingService = null;
    private ProfileMgtService profileMgtService = null;


    private RealmService realmService = null;

    private GatewayServiceHolder() {
        setNanoTimeReference(System.nanoTime());
        setUnixTimeReference(System.currentTimeMillis());
    }

    public static GatewayServiceHolder getInstance() {
        return instance;
    }

    public List<AuthenticationHandler> getAuthenticationHandlers() {
        return authenticationHandlers;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public ClaimResolvingService getClaimResolvingService() {
        return claimResolvingService;
    }

    public void setClaimResolvingService(ClaimResolvingService claimResolvingService) {
        this.claimResolvingService = claimResolvingService;
    }

    public FederatedApplicationAuthenticator getFederatedApplicationAuthenticator(String name) {
        FederatedApplicationAuthenticator federatedApplicationAuthenticator = null;
        for (FederatedApplicationAuthenticator tmpFederatedApplicationAuthenticator :
                federatedApplicationAuthenticators) {
            if (tmpFederatedApplicationAuthenticator.getName().equals(name)) {
                federatedApplicationAuthenticator = tmpFederatedApplicationAuthenticator;
                break;
            }
        }
        return federatedApplicationAuthenticator;
    }

    public List<FederatedApplicationAuthenticator> getFederatedApplicationAuthenticators() {
        return federatedApplicationAuthenticators;
    }

    public List<GatewayProcessor> getGatewayProcessors() {
        return gatewayProcessors;
    }

    public List<GatewayRequestBuilderFactory> getHttpIdentityRequestFactories() {
        return httpIdentityRequestFactories;
    }

    public List<GatewayResponseBuilderFactory> getHttpIdentityResponseFactories() {
        return httpIdentityResponseFactories;
    }

    public LocalApplicationAuthenticator getLocalApplicationAuthenticator(String name) {
        LocalApplicationAuthenticator localApplicationAuthenticator = null;
        for (LocalApplicationAuthenticator tmpLocalApplicationAuthenticator : localApplicationAuthenticators) {
            if (tmpLocalApplicationAuthenticator.getName().equals(name)) {
                localApplicationAuthenticator = tmpLocalApplicationAuthenticator;
                break;
            }
        }
        return localApplicationAuthenticator;
    }

    public List<LocalApplicationAuthenticator> getLocalApplicationAuthenticators() {
        return localApplicationAuthenticators;
    }

    public long getNanoTimeReference() {
        return nanoTimeReference;
    }

    private void setNanoTimeReference(long nanoTimeReference) {
        this.nanoTimeReference = nanoTimeReference;
    }

    public ProfileMgtService getProfileMgtService() {
        return profileMgtService;
    }

    public void setProfileMgtService(ProfileMgtService profileMgtService) {
        this.profileMgtService = profileMgtService;
    }

    public List<AbstractRequestValidator> getRequestHandlers() {
        return requestHandlers;
    }

    public RequestPathApplicationAuthenticator getRequestPathApplicationAuthenticator(String name) {
        RequestPathApplicationAuthenticator requestPathApplicationAuthenticator = null;
        for (RequestPathApplicationAuthenticator authenticator : requestPathApplicationAuthenticators) {
            if (authenticator.getName().equals(name)) {
                requestPathApplicationAuthenticator = authenticator;
                break;
            }
        }
        return requestPathApplicationAuthenticator;
    }

    public List<RequestPathApplicationAuthenticator> getRequestPathApplicationAuthenticators() {
        return requestPathApplicationAuthenticators;
    }

    public List<AbstractResponseHandler> getResponseHandlers() {
        return responseHandlers;
    }

    public List<AbstractSessionHandler> getSessionHandlers() {
        return sessionHandlers;
    }

    public void setSessionHandlers(List<AbstractSessionHandler> sessionHandlers) {
        this.sessionHandlers = sessionHandlers;
    }

    public List<AuthenticationStepHandler> getAuthenticationStepHandlers() {
        return authenticationStepHandlers;
    }

    public long getUnixTimeReference() {
        return unixTimeReference;
    }

    private void setUnixTimeReference(long unixTimeReference) {
        this.unixTimeReference = unixTimeReference;
    }

    public List<AbstractSequenceBuildFactory> getSequenceBuildFactories() {
        return sequenceBuildFactories;
    }

    public List<AbstractExecutionHandler> getExecutionHandlers() {
        return executionHandlers;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }
}

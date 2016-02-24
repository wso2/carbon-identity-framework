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

package org.wso2.carbon.identity.application.authentication.framework.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.inbound.InboundAuthenticationRequestBuilder;
import org.wso2.carbon.identity.application.authentication.framework.inbound.InboundAuthenticationRequestProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.InboundAuthenticationResponseProcessor;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

public class FrameworkServiceDataHolder {
    private static FrameworkServiceDataHolder instance = new FrameworkServiceDataHolder();

    private BundleContext bundleContext = null;
    private RealmService realmService = null;
    private RegistryService registryService = null;
    private List<ApplicationAuthenticator> authenticators = new ArrayList<>();
    private long nanoTimeReference = 0;
    private long unixTimeReference = 0;
    private List<InboundAuthenticationRequestProcessor> inboundAuthenticationRequestProcessors = new ArrayList<InboundAuthenticationRequestProcessor>();
    private List<InboundAuthenticationRequestBuilder> inboundAuthenticationRequestBuilders = new ArrayList<InboundAuthenticationRequestBuilder>();
    private List<InboundAuthenticationResponseProcessor> inboundAuthenticationResponseProcessors = new ArrayList<InboundAuthenticationResponseProcessor>();

    private FrameworkServiceDataHolder() {
        setNanoTimeReference(System.nanoTime());
        setUnixTimeReference(System.currentTimeMillis());
    }

    public static FrameworkServiceDataHolder getInstance() {
        return instance;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public List<ApplicationAuthenticator> getAuthenticators() {
        return authenticators;
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

    public List<InboundAuthenticationRequestBuilder> getInboundAuthenticationRequestBuilders() {
        return inboundAuthenticationRequestBuilders;
    }

    public void setInboundAuthenticationRequestBuilders(
            List<InboundAuthenticationRequestBuilder> inboundAuthenticationRequestBuilders) {
        this.inboundAuthenticationRequestBuilders = inboundAuthenticationRequestBuilders;
    }

    public List<InboundAuthenticationRequestProcessor> getInboundAuthenticationRequestProcessors() {
        return inboundAuthenticationRequestProcessors;
    }

    public void setInboundAuthenticationRequestProcessors(
            List<InboundAuthenticationRequestProcessor> inboundAuthenticationRequestProcessors) {
        this.inboundAuthenticationRequestProcessors = inboundAuthenticationRequestProcessors;
    }

    public List<InboundAuthenticationResponseProcessor> getInboundAuthenticationResponseProcessors() {
        return inboundAuthenticationResponseProcessors;
    }

    public void setInboundAuthenticationResponseProcessors(
            List<InboundAuthenticationResponseProcessor> inboundAuthenticationResponseProcessors) {
        this.inboundAuthenticationResponseProcessors = inboundAuthenticationResponseProcessors;
    }
}

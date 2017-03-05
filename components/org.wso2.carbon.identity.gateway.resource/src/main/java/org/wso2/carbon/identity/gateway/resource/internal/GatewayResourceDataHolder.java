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

package org.wso2.carbon.identity.gateway.resource.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.gateway.api.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;

import java.util.ArrayList;
import java.util.List;

public class GatewayResourceDataHolder {
    private static GatewayResourceDataHolder instance = new GatewayResourceDataHolder();
    GatewayRequestBuilderFactory defaultGatewayRequestBuilderFactory = null;
    GatewayResponseBuilderFactory defaultGatewayResponseBuilderFactory = null;
    private BundleContext bundleContext = null;
    private long nanoTimeReference = 0;
    private long unixTimeReference = 0;
    private List<GatewayProcessor> gatewayProcessors = new ArrayList<GatewayProcessor>();
    private List<GatewayRequestBuilderFactory> httpIdentityRequestFactories
            = new ArrayList<GatewayRequestBuilderFactory>();
    private List<GatewayResponseBuilderFactory> httpIdentityResponseFactories = new ArrayList<>();
    //Framework handlers


    private GatewayResourceDataHolder() {
        setNanoTimeReference(System.nanoTime());
        setUnixTimeReference(System.currentTimeMillis());
    }

    public static GatewayResourceDataHolder getInstance() {
        return instance;
    }


    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public GatewayRequestBuilderFactory getDefaultGatewayRequestBuilderFactory() {
        return defaultGatewayRequestBuilderFactory;
    }

    public void setDefaultGatewayRequestBuilderFactory(GatewayRequestBuilderFactory
                                                               defaultGatewayRequestBuilderFactory) {
        this.defaultGatewayRequestBuilderFactory = defaultGatewayRequestBuilderFactory;
    }

    public GatewayResponseBuilderFactory getDefaultGatewayResponseBuilderFactory() {
        return defaultGatewayResponseBuilderFactory;
    }

    public void setDefaultGatewayResponseBuilderFactory(GatewayResponseBuilderFactory
                                                                defaultGatewayResponseBuilderFactory) {
        this.defaultGatewayResponseBuilderFactory = defaultGatewayResponseBuilderFactory;
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
}

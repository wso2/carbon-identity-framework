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

import org.wso2.carbon.identity.gateway.api.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * GatewayResourceDataHolder to hold the services references.
 */
public class GatewayResourceDataHolder {

    private static GatewayResourceDataHolder instance = new GatewayResourceDataHolder();

    private List<GatewayProcessor> gatewayProcessors = new ArrayList<GatewayProcessor>();
    private List<GatewayRequestBuilderFactory> httpIdentityRequestFactories
            = new ArrayList<GatewayRequestBuilderFactory>();
    private List<GatewayResponseBuilderFactory> httpIdentityResponseFactories = new ArrayList<>();


    private GatewayResourceDataHolder() {
    }

    public static GatewayResourceDataHolder getInstance() {
        return instance;
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
}

/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.resource.internal;

import org.wso2.carbon.identity.framework.util.FrameworkUtil;
import org.wso2.carbon.identity.gateway.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.resource.factory.GatewayRequestFactory;
import org.wso2.carbon.identity.gateway.resource.factory.GatewayResponseFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * DataHolder to hold service instances required by org.wso2.carbon.identity.gateway.resource component.
 *
 * @since 1.0.0
 */
public class DataHolder {

    private static DataHolder instance = new DataHolder();

    private List<GatewayRequestFactory> requestFactoryList = new ArrayList<>();
    private List<GatewayResponseFactory> responseFactoryList = new ArrayList<>();
    private List<GatewayProcessor> gatewayProcessors = new ArrayList<>();
//    private IdentityProcessCoordinator processCoordinator;

    private Comparator<GatewayRequestFactory> requestFactoryComparator =
            (factory1, factory2) -> FrameworkUtil.comparePriory(factory1.getPriority(), factory2.getPriority());

    private Comparator<GatewayResponseFactory> responseFactoryComparator =
            (factory1, factory2) -> FrameworkUtil.comparePriory(factory1.getPriority(), factory2.getPriority());

    private Comparator<GatewayProcessor> gatewayComparator =
            (processor1, processor2) -> FrameworkUtil.comparePriory(processor1.getPriority(), processor2.getPriority());

    private DataHolder() {

    }

    /**
     * This returns the DataHolder instance.
     *
     * @return The DataHolder instance of this singleton class
     */
    public static DataHolder getInstance() {

        return instance;
    }


    /**
     * Add a {@link GatewayRequestFactory} service instance. This will be used by the ServiceComponent
     * when a @{@link GatewayRequestFactory} registers.
     *
     * @param requestBuilderFactory @GatewayRequestFactory instance added.
     */
    public void addRequestFactory(GatewayRequestFactory requestBuilderFactory) {

        requestFactoryList.add(requestBuilderFactory);
        Collections.sort(requestFactoryList, requestFactoryComparator);
    }


    /**
     * Remove a {@link GatewayRequestFactory} service instance. This will be used by the ServiceComponent
     * when a @{@link GatewayRequestFactory} unregisters.
     *
     * @param requestBuilderFactory @GatewayRequestFactory removed.
     */
    public void removeRequestFactory(GatewayRequestFactory requestBuilderFactory) {

        requestFactoryList.remove(requestBuilderFactory);
    }


    /**
     * Return a list registered @{@link GatewayRequestFactory} services.
     *
     * @return list of @{@link GatewayRequestFactory} services.
     */
    public List<GatewayRequestFactory> getRequestFactoryList() {

        return requestFactoryList;
    }


    /**
     * Add a {@link GatewayResponseFactory} service instance. This will be used by the ServiceComponent
     * when a @{@link GatewayResponseFactory} registers.
     *
     * @param responseBuilderFactory @{@link GatewayResponseFactory} instance added.
     */
    public void addResponseFactory(GatewayResponseFactory responseBuilderFactory) {

        responseFactoryList.add(responseBuilderFactory);
        Collections.sort(responseFactoryList, responseFactoryComparator);
    }

    /**
     * Remove a {@link GatewayResponseFactory} service instance. This will be used by the ServiceComponent
     * when a @{@link GatewayResponseFactory} un-registers.
     *
     * @param responseBuilderFactory @{@link GatewayResponseFactory} instance removed.
     */
    public void removeResponseFactory(GatewayResponseFactory responseBuilderFactory) {

        responseFactoryList.remove(responseBuilderFactory);
    }

    /**
     * Return a list registered @{@link GatewayResponseFactory} services.
     *
     * @return list of @{@link GatewayResponseFactory} services.
     */
    public List<GatewayResponseFactory> getResponseFactoryList() {

        return responseFactoryList;
    }

    /**
     * Add a {@link GatewayProcessor} service instance. This will be used by the ServiceComponent
     * when a @{@link GatewayProcessor} registers.
     *
     * @param gatewayProcessor @{@link GatewayProcessor} instance added.
     */
    public void addIdentityProcessor(GatewayProcessor gatewayProcessor) {

        gatewayProcessors.add(gatewayProcessor);
        Collections.sort(gatewayProcessors, gatewayComparator);
    }


    /**
     * Return a list registered @{@link GatewayProcessor} services.
     *
     * @return list of @{@link GatewayProcessor} services.
     */
    public List<GatewayProcessor> getGatewayProcessors() {

        return gatewayProcessors;
    }
}

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

import org.wso2.carbon.identity.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.IdentityProcessCoordinator;
import org.wso2.carbon.identity.framework.util.FrameworkUtil;
import org.wso2.carbon.identity.gateway.resource.GatewayRequestFactory;
import org.wso2.carbon.identity.gateway.resource.GatewayResponseBuilderFactory;

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
    private List<GatewayResponseBuilderFactory> responseFactoryList = new ArrayList<>();
    private IdentityProcessCoordinator processCoordinator;

    private Comparator<GatewayRequestFactory> requestFactoryComparator =
            (factory1, factory2) -> FrameworkUtil.comparePriory(factory1.getPriority(), factory2.getPriority());

    private Comparator<GatewayResponseBuilderFactory> responseFactoryComparator =
            (factory1, factory2) -> FrameworkUtil.comparePriory(factory1.getPriority(), factory2.getPriority());

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
     * Add a {@link GatewayResponseBuilderFactory} service instance. This will be used by the ServiceComponent
     * when a @{@link GatewayResponseBuilderFactory} registers.
     *
     * @param responseBuilderFactory @{@link GatewayResponseBuilderFactory} instance added.
     */
    public void addResponseFactory(GatewayResponseBuilderFactory responseBuilderFactory) {

        responseFactoryList.add(responseBuilderFactory);
        Collections.sort(responseFactoryList, responseFactoryComparator);
    }

    /**
     * Remove a {@link GatewayResponseBuilderFactory} service instance. This will be used by the ServiceComponent
     * when a @{@link GatewayResponseBuilderFactory} un-registers.
     *
     * @param responseBuilderFactory @{@link GatewayResponseBuilderFactory} instance removed.
     */
    public void removeResponseFactory(GatewayResponseBuilderFactory responseBuilderFactory) {

        responseFactoryList.remove(responseBuilderFactory);
    }

    /**
     * Return a list registered @{@link GatewayResponseBuilderFactory} services.
     *
     * @return list of @{@link GatewayResponseBuilderFactory} services.
     */
    public List<GatewayResponseBuilderFactory> getResponseFactoryList() {

        return responseFactoryList;
    }

    /**
     * Get the registered @{@link IdentityProcessCoordinator} OSGi service for consumption.
     *
     * @return IdentityProcessCoordinator instance set by the ServiceComponent.
     */
    public IdentityProcessCoordinator getProcessCoordinator() {

        if (processCoordinator == null) {
            throw FrameworkRuntimeException.error("Identity Process Coordinator cannot be null.");
        }
        return processCoordinator;
    }

    /**
     * Set the {@link IdentityProcessCoordinator} service instance. This will be used by the ServiceComponent
     * when a @{@link IdentityProcessCoordinator} registers.
     *
     * @param processCoordinator {@link IdentityProcessCoordinator} service registered.
     */
    public void setProcessCoordinator(IdentityProcessCoordinator processCoordinator) {

        this.processCoordinator = processCoordinator;
    }

}

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

import org.wso2.carbon.identity.framework.IdentityProcessCoordinator;
import org.wso2.carbon.identity.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.resource.MSF4JIdentityRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.resource.MSF4JResponseBuilderFactory;

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

    private List<MSF4JIdentityRequestBuilderFactory> requestFactoryList = new ArrayList<>();
    private List<MSF4JResponseBuilderFactory> responseFactoryList = new ArrayList<>();
    private IdentityProcessCoordinator processCoordinator;

    private Comparator<MSF4JIdentityRequestBuilderFactory> requestFactoryComparator =
            (factory1, factory2) -> FrameworkUtil.comparePriory(factory1.getPriority(), factory2.getPriority());

    private Comparator<MSF4JResponseBuilderFactory> responseFactoryComparator =
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
     * Add a {@link MSF4JIdentityRequestBuilderFactory} service instance. This will be used by the ServiceComponent
     * when a @{@link MSF4JIdentityRequestBuilderFactory} registers.
     *
     * @param requestBuilderFactory @MSF4JIdentityRequestBuilderFactory instance added.
     */
    public void addRequestFactory(MSF4JIdentityRequestBuilderFactory requestBuilderFactory) {

        requestFactoryList.add(requestBuilderFactory);
        Collections.sort(requestFactoryList, requestFactoryComparator);
    }


    /**
     * Remove a {@link MSF4JIdentityRequestBuilderFactory} service instance. This will be used by the ServiceComponent
     * when a @{@link MSF4JIdentityRequestBuilderFactory} unregisters.
     *
     * @param requestBuilderFactory @MSF4JIdentityRequestBuilderFactory removed.
     */
    public void removeRequestFactory(MSF4JIdentityRequestBuilderFactory requestBuilderFactory) {

        requestFactoryList.remove(requestBuilderFactory);
    }


    /**
     * Return a list registered @{@link MSF4JIdentityRequestBuilderFactory} services.
     *
     * @return list of @{@link MSF4JIdentityRequestBuilderFactory} services.
     */
    public List<MSF4JIdentityRequestBuilderFactory> getRequestFactoryList() {

        return requestFactoryList;
    }


    /**
     * Add a {@link MSF4JResponseBuilderFactory} service instance. This will be used by the ServiceComponent
     * when a @{@link MSF4JResponseBuilderFactory} registers.
     *
     * @param responseBuilderFactory @{@link MSF4JResponseBuilderFactory} instance added.
     */
    public void addResponseFactory(MSF4JResponseBuilderFactory responseBuilderFactory) {

        responseFactoryList.add(responseBuilderFactory);
        Collections.sort(responseFactoryList, responseFactoryComparator);
    }

    /**
     * Remove a {@link MSF4JResponseBuilderFactory} service instance. This will be used by the ServiceComponent
     * when a @{@link MSF4JResponseBuilderFactory} un-registers.
     *
     * @param responseBuilderFactory @{@link MSF4JResponseBuilderFactory} instance removed.
     */
    public void removeResponseFactory(MSF4JResponseBuilderFactory responseBuilderFactory) {

        responseFactoryList.remove(responseBuilderFactory);
    }

    /**
     * Return a list registered @{@link MSF4JResponseBuilderFactory} services.
     *
     * @return list of @{@link MSF4JResponseBuilderFactory} services.
     */
    public List<MSF4JResponseBuilderFactory> getResponseFactoryList() {

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

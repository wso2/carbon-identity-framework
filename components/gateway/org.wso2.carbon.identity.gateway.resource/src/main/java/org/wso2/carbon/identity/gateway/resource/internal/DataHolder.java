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
import org.wso2.carbon.identity.framework.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.response.factory.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.framework.util.FrameworkUtil;
import org.wso2.carbon.identity.gateway.resource.MSF4JIdentityRequestFactory;
import org.wso2.carbon.kernel.CarbonRuntime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * DataHolder to hold org.wso2.carbon.kernel.CarbonRuntime instance referenced through
 * org.wso2.carbon.helloworld.internal.ServiceComponent.
 *
 * @since 1.0.0
 */
public class DataHolder {
    Logger logger = Logger.getLogger(DataHolder.class.getName());

    private static DataHolder instance = new DataHolder();
    private CarbonRuntime carbonRuntime;

    private List<MSF4JIdentityRequestFactory> requestFactoryList = new ArrayList<>();
    private List<HttpIdentityResponseFactory> responseFactoryList = new ArrayList<>();
    private IdentityProcessCoordinator processCoordinator;

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
     * Returns the CarbonRuntime service which gets set through a service component.
     *
     * @return CarbonRuntime Service
     */
    public CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    /**
     * This method is for setting the CarbonRuntime service. This method is used by
     * ServiceComponent.
     *
     * @param carbonRuntime The reference being passed through ServiceComponent
     */
    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
    }


    public void addRequestFactory(MSF4JIdentityRequestFactory requestFactory) {
        requestFactoryList.add(requestFactory);
        Collections.sort(requestFactoryList, requestFactoryComparator);
    }

    public void removeRequestFactory(MSF4JIdentityRequestFactory requestFactory) {
        requestFactoryList.remove(requestFactory);
    }

    public List<MSF4JIdentityRequestFactory> getRequestFactoryList() {
        return requestFactoryList;
    }


    public void addResponseFactory(HttpIdentityResponseFactory httpIdentityResponseFactory) {
        responseFactoryList.add(httpIdentityResponseFactory);
        Collections.sort(responseFactoryList, responseFactoryComparator);
    }

    public void removeResponseFactory(HttpIdentityResponseFactory httpIdentityResponseFactory) {
        responseFactoryList.remove(httpIdentityResponseFactory);
    }

    public List<HttpIdentityResponseFactory> getResponseFactoryList() {
        return responseFactoryList;
    }

    public IdentityProcessCoordinator getProcessCoordinator() {
        if (processCoordinator == null) {
            throw FrameworkRuntimeException.error("Identity Process Co-ordinator cannot be null.");
        }
        return processCoordinator;
    }

    public void setProcessCoordinator(IdentityProcessCoordinator processCoordinator) {
        this.processCoordinator = processCoordinator;
    }

    private Comparator<MSF4JIdentityRequestFactory> requestFactoryComparator =
            (factory1, factory2) -> FrameworkUtil.comparePriory(factory1.getPriority(), factory2.getPriority());

    private Comparator<HttpIdentityResponseFactory> responseFactoryComparator =
            (factory1, factory2) -> FrameworkUtil.comparePriory(factory1.getPriority(), factory2.getPriority());
}

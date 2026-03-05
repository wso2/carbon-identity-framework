/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.flow.mgt.internal;

import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.flow.mgt.FlowUpdateInterceptor;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A singleton class to hold the data of the flow management service.
 */
public class FlowMgtServiceDataHolder {

    private OrganizationManager organizationManager;
    private OrgResourceResolverService orgResourceResolverService;
    private ConfigurationManager configurationManager;
    private final List<FlowUpdateInterceptor> flowUpdateInterceptors = new ArrayList<>();

    private static final FlowMgtServiceDataHolder INSTANCE = new FlowMgtServiceDataHolder();

    private FlowMgtServiceDataHolder() {

    }

    public static FlowMgtServiceDataHolder getInstance() {

        return INSTANCE;
    }

    public OrganizationManager getOrganizationManager() {

        return organizationManager;
    }

    public void setOrganizationManager(OrganizationManager organizationManager) {

        this.organizationManager = organizationManager;
    }

    public OrgResourceResolverService getOrgResourceResolverService() {

        return orgResourceResolverService;
    }

    public void setOrgResourceResolverService(OrgResourceResolverService orgResourceResolverService) {

        this.orgResourceResolverService = orgResourceResolverService;
    }

    public ConfigurationManager getConfigurationManager() {

        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
    }

    public List<FlowUpdateInterceptor> getFlowUpdateInterceptors() {

        return Collections.unmodifiableList(flowUpdateInterceptors);
    }

    public void addFlowUpdateInterceptor(FlowUpdateInterceptor interceptor) {

        flowUpdateInterceptors.add(interceptor);
    }

    public void removeFlowUpdateInterceptor(FlowUpdateInterceptor interceptor) {

        flowUpdateInterceptors.remove(interceptor);
    }
}

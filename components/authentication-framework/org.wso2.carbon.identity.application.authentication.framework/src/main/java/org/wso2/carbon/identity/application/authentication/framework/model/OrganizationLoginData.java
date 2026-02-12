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

package org.wso2.carbon.identity.application.authentication.framework.model;

import java.io.Serializable;

/**
 * Holds the organization login related data during an authentication flow.
 */
public class OrganizationLoginData implements Serializable {

    private static final long serialVersionUID = -2115398895048768726L;

    // Root organization tenant domain.
    private String rootOrganizationTenantDomain;
    // Shared application id used in shared application login.
    private String sharedApplicationId;
    // Accessing organization data in shared application login.
    private OrganizationData accessingOrganization;

    public OrganizationData getAccessingOrganization() {

        return this.accessingOrganization;
    }

    public void setAccessingOrganization(OrganizationData accessingOrganization) {

        this.accessingOrganization = accessingOrganization;
    }

    public String getSharedApplicationId() {

        return this.sharedApplicationId;
    }

    public void setSharedApplicationId(String sharedApplicationId) {

        this.sharedApplicationId = sharedApplicationId;
    }

    public String getRootOrganizationTenantDomain() {

        return this.rootOrganizationTenantDomain;
    }

    public void setRootOrganizationTenantDomain(String rootOrganizationTenantDomain) {

        this.rootOrganizationTenantDomain = rootOrganizationTenantDomain;
    }
}

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

package org.wso2.carbon.identity.user.pre.update.password.action.internal.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.Tenant;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.component.PreUpdatePasswordActionServiceComponentHolder;

import java.util.Collections;
import java.util.Map;

/**
 * Utility class for organization management related operations.
 */
public class OrganizationMgtUtil {

    private OrganizationMgtUtil() {

    }

    public static boolean isOrganization() throws ActionExecutionRequestBuilderException {

        try {
            int tenantId = IdentityContext.getThreadLocalCarbonContext().getTenantId();
            return OrganizationManagementUtil.isOrganization(tenantId);
        } catch (OrganizationManagementException e) {
            throw new ActionExecutionRequestBuilderException("Error while checking if the tenant is an organization.",
                    e);
        }
    }

    public static Organization getOrganization() throws ActionExecutionRequestBuilderException {

        try {
            String organizationId = getOrganizationId();
            BasicOrganization basicOrganization = getOrganizationBasicInfo(organizationId);

            return new Organization.Builder()
                    .id(organizationId)
                    .name(basicOrganization.getName())
                    .orgHandle(basicOrganization.getOrganizationHandle())
                    .depth(getOrganizationDepthInHierarchy(organizationId))
                    .build();
        } catch (OrganizationManagementException e) {
            throw new ActionExecutionRequestBuilderException("Error while retrieving organization information." , e);
        }
    }

    public static Tenant resolveTenant(String organizationId) throws ActionExecutionRequestBuilderException {

        try {
            String tenantDomain = getRootOrgTenantDomain(organizationId);
            String tenantId = String.valueOf(IdentityTenantUtil.getTenantId(tenantDomain));
            return new Tenant(tenantId, tenantDomain);
        } catch (OrganizationManagementException | IdentityRuntimeException e) {
            throw new ActionExecutionRequestBuilderException("Error while resolving tenant for organization ID: "
                    + organizationId, e);
        }
    }

    private static int getOrganizationDepthInHierarchy(String organizationId) throws OrganizationManagementException {

        return getOrganizationManager().getOrganizationDepthInHierarchy(organizationId);
    }

    private static BasicOrganization getOrganizationBasicInfo(String organizationId)
            throws OrganizationManagementException {

        Map<String, BasicOrganization> orgList = getOrganizationManager()
                .getBasicOrganizationDetailsByOrgIDs(Collections.singletonList(organizationId));
        if (orgList.isEmpty()) {
            throw new OrganizationManagementException("No organization found for the organization ID: "
                    + organizationId);
        }
        return orgList.get(organizationId);
    }

    private static String getOrganizationId() throws OrganizationManagementException {

        String organizationId = IdentityContext.getThreadLocalCarbonContext().getOrganizationId();
        if (StringUtils.isBlank(organizationId)) {
            String tenantDomain = IdentityContext.getThreadLocalCarbonContext().getTenantDomain();
            organizationId = getOrganizationManager().resolveOrganizationId(tenantDomain);
        }
        return organizationId;
    }

    private static String getRootOrgTenantDomain(String organizationId) throws OrganizationManagementException {

        String rootOrganizationId = getOrganizationManager().getPrimaryOrganizationId(organizationId);
        return getOrganizationManager().resolveTenantDomain(rootOrganizationId);
    }

    private static OrganizationManager getOrganizationManager() {

        return PreUpdatePasswordActionServiceComponentHolder.getInstance().getOrganizationManager();
    }
}

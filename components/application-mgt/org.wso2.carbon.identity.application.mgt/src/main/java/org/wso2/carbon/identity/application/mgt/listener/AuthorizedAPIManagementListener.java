/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementServiceImpl;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.AbstractIdentityTenantMgtListener;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;

/**
 * Authorized API Management listener class.
 */
public class AuthorizedAPIManagementListener extends AbstractIdentityTenantMgtListener {

    private static final Log LOG = LogFactory.getLog(AuthorizedAPIManagementListener.class);

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfo) {

        if (!isEnable()) {
            LOG.debug("Authorized API Management related AuthorizedAPIManagementListener is not enabled.");
            return;
        }

        int tenantId = tenantInfo.getTenantId();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Authorized API Management related AuthorizedAPIManagementListener fired for tenant " +
                    "creation for Tenant ID: " + tenantId);
        }

        try {
            if (OrganizationManagementUtil.isOrganization(tenantId)) {
                return;
            }
            authorizeSystemAPIConsole(tenantInfo.getTenantDomain());
        } catch (OrganizationManagementException e) {
            LOG.error("Error while registering system API resources in tenant: " + tenantInfo.getTenantDomain());
        }
    }

    private void authorizeSystemAPIConsole(String tenantDomain) {

        try {
            ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
            ApplicationBasicInfo applicationBasicInfo = applicationManagementService.getApplicationBasicInfoByName(
                    ApplicationConstants.CONSOLE_APPLICATION_NAME, tenantDomain);
            if (applicationBasicInfo == null) {
                LOG.error("Error while authorizing system API console. Console application not found.");
                return;
            }
            AuthorizedAPIManagementService authorizedAPIManagementService = new AuthorizedAPIManagementServiceImpl();
            List<AuthorizedAPI> authorizedAPIs = authorizedAPIManagementService.getAuthorizedAPIs(
                    applicationBasicInfo.getApplicationResourceId(), tenantDomain);
            // Return if the system APIs are already authorized for the console application.
            if (!authorizedAPIs.isEmpty()) {
                LOG.debug("System APIs are already authorized for the console application.");
                return;
            }
            // Fetch the system API resources count.
            int systemAPICount = ApplicationManagementServiceComponentHolder.getInstance()
                    .getAPIResourceManager().getAPIResources(null, null, 1, "type eq SYSTEM",
                            "ASC", tenantDomain).getTotalCount();
            // Fetch all system APIs.
            List<APIResource> apiResources = ApplicationManagementServiceComponentHolder.getInstance()
                    .getAPIResourceManager().getAPIResources(null, null, systemAPICount, "type eq SYSTEM",
                            "ASC", tenantDomain).getAPIResources();
            if (apiResources.isEmpty()) {
                LOG.error("Error while authorizing system API console. System APIs not found.");
                return;
            }
            for (APIResource apiResource : apiResources) {
                List<Scope> scopes = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getAPIScopesById(apiResource.getId(), tenantDomain);
                AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                        .apiId(apiResource.getId())
                        .appId(applicationBasicInfo.getApplicationResourceId())
                        .scopes(scopes)
                        .policyId(APIResourceManagementConstants.RBAC_AUTHORIZATION)
                        .build();
                authorizedAPIManagementService.addAuthorizedAPI(applicationBasicInfo.getApplicationResourceId(),
                        authorizedAPI, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            }
            LOG.debug("System APIs are authorized for the console application in " + tenantDomain);
        } catch (Throwable e) {
            LOG.error("Error while authorizing system API to the console application.", e);
        }
    }
}

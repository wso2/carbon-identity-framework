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
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementServiceImpl;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.util.List;

/**
 * Authorized API Management listener class.`
 */
public class AuthorizedAPIManagementListener extends AbstractApplicationMgtListener {

    private static final Log LOG = LogFactory.getLog(AuthorizedAPIManagementListener.class);

    @Override
    public boolean doPostCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        if (!isEnable()) {
            LOG.debug("Authorized API Management related AuthorizedAPIManagementListener is not enabled.");
            return true;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Authorized API Management related AuthorizedAPIManagementListener fired for tenant " +
                    "creation for Tenant: " + tenantDomain);
        }

        String appName = serviceProvider.getApplicationName();
        // Return if the application is not console or my account.
        if (!isConsole(appName) && !isMyAccount(appName)) {
            return true;
        }

        try {
            if (OrganizationManagementUtil.isOrganization(tenantDomain)) {
                return true;
            }
            if (isConsole(appName)) {
                authorizeSystemAPIToConsole(tenantDomain);
            }

        } catch (OrganizationManagementException e) {
            LOG.error("Error while registering system API resources in tenant: " + tenantDomain);
        }
        return true;
    }

    /**
     * Check whether the application is Console.
     *
     * @param name Application name.
     * @return True if the application is Console.
     */
    private boolean isConsole(String name) {

        return ApplicationConstants.CONSOLE_APPLICATION_NAME.equals(name);
    }

    /**
     * Check whether the application is My Account.
     *
     * @param name Application name.
     * @return True if the application is My Account.
     */
    private boolean isMyAccount(String name) {

        return ApplicationConstants.MY_ACCOUNT_APPLICATION_NAME.equals(name);
    }

    /**
     * Authorize system APIs to the Console application.
     *
     * @param tenantDomain Tenant domain.
     */
    private void authorizeSystemAPIToConsole(String tenantDomain) {

        try {
            ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
            ApplicationBasicInfo applicationBasicInfo = applicationManagementService.getApplicationBasicInfoByName(
                    ApplicationConstants.CONSOLE_APPLICATION_NAME, tenantDomain);
            if (applicationBasicInfo == null) {
                LOG.error("Error while authorizing system API to the Console. Console application not found in tenant: "
                        + tenantDomain);
                return;
            }
            AuthorizedAPIManagementService authorizedAPIManagementService = new AuthorizedAPIManagementServiceImpl();
            List<AuthorizedAPI> authorizedAPIs = authorizedAPIManagementService.getAuthorizedAPIs(
                    applicationBasicInfo.getApplicationResourceId(), tenantDomain);
            // Return if the system APIs are already authorized for the console application.
            if (!authorizedAPIs.isEmpty()) {
                LOG.debug("System APIs are already authorized for the Console application in tenant: "
                        + tenantDomain);
                return;
            }

            // Fetch all system APIs.
            List<APIResource> apiResources = APIResourceManagementUtil.getSystemAPIs(tenantDomain);
            if (apiResources.isEmpty()) {
                LOG.error("Error while authorizing system APIs to the Console. System APIs not found in tenant: "
                        + tenantDomain);
                return;
            }
            for (APIResource apiResource : apiResources) {
                String policyId = APIResourceManagementConstants.RBAC_AUTHORIZATION;
                if (APIResourceManagementConstants.ME_API.equals(apiResource.getName())) {
                    policyId = APIResourceManagementConstants.NO_POLICY;
                }
                List<Scope> scopes = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getAPIScopesById(apiResource.getId(), tenantDomain);
                AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                        .apiId(apiResource.getId())
                        .appId(applicationBasicInfo.getApplicationResourceId())
                        .scopes(scopes)
                        .policyId(policyId)
                        .build();
                authorizedAPIManagementService.addAuthorizedAPI(applicationBasicInfo.getApplicationResourceId(),
                        authorizedAPI, tenantDomain);
            }
            LOG.debug("System APIs are authorized for the Console application in " + tenantDomain);
        } catch (Throwable e) {
            LOG.error("Error while authorizing system APIs to the Console application.", e);
        }
    }

    @Override
    public int getDefaultOrderId() {

        return 211;
    }

    @Override
    public boolean isEnable() {

        return true;
    }
}

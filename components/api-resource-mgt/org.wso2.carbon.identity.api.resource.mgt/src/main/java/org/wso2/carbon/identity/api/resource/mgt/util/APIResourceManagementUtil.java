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

package org.wso2.carbon.identity.api.resource.mgt.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManagerImpl;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtClientException;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtServerException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for API Resource Management.
 */
public class APIResourceManagementUtil {

    private static final Log LOG = LogFactory.getLog(APIResourceManagementUtil.class);

    /**
     * Handle API Resource Management client exceptions.
     *
     * @param error Error message.
     * @param data  Data.
     * @return APIResourceMgtClientException.
     */
    public static APIResourceMgtClientException handleClientException(
            APIResourceManagementConstants.ErrorMessages error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new APIResourceMgtClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Handle API Resource Management server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data.
     * @return APIResourceMgtServerException.
     */
    public static APIResourceMgtServerException handleServerException(
            APIResourceManagementConstants.ErrorMessages error, Throwable e, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }

        return new APIResourceMgtServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Fetch the configuration from the XML file and register the system API in the given tenant.
     */
    public static void addSystemAPIs() {

        try {
            String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            Map<String, APIResource> configs = APIResourceManagementConfigBuilder.getInstance()
                    .getAPIResourceMgtConfigurations();
            if (!isSystemAPIExist(tenantDomain)) {
                LOG.debug("Registering system API resources in the server.");
                registerAPIResources(new ArrayList<>(configs.values()), tenantDomain);
            } else {
                LOG.debug("System APIs are already registered in the server. Applying the latest configurations.");
                List<APIResource> systemAPIs = getSystemAPIs(tenantDomain);
                for (APIResource systemAPI : systemAPIs) {
                    if (configs.containsKey(systemAPI.getIdentifier())) {
                        configs.remove(systemAPI.getIdentifier());
                    } else {
                        String apiId = APIResourceManagerImpl.getInstance().getAPIResourceByIdentifier(
                                systemAPI.getIdentifier(), tenantDomain).getId();
                        APIResourceManagerImpl.getInstance().deleteAPIResourceById(apiId, tenantDomain);
                    }
                }
                registerAPIResources(new ArrayList<>(configs.values()), tenantDomain);
            }

            LOG.debug("System APIs successfully registered in tenant domain: " + tenantDomain);
        } catch (APIResourceMgtException e) {
            LOG.error("Error while registering system API resources in the server.", e);
        }
    }

    private static void registerAPIResources(List<APIResource> apiResources, String tenantDomain) {

        for (APIResource apiResource : apiResources) {
            if (apiResource != null) {
                try {
                    APIResourceManagerImpl.getInstance().addAPIResource(apiResource, tenantDomain);
                } catch (APIResourceMgtException e) {
                    LOG.error("Error while registering system API resources in the tenant: " + tenantDomain);
                }
            }
        }
    }

    /**
     * Fetch all system APIs registered in the tenant.
     *
     * @param tenantDomain tenant domain.
     * @return List of system APIs.
     * @throws APIResourceMgtException if an error occurs while fetching system APIs.
     */
    public static List<APIResource> getSystemAPIs(String tenantDomain) throws APIResourceMgtException {

        List<APIResource> systemAPIs = new ArrayList<>();
        // Get APIs with SYSTEM type.
        int systemAPICount = APIResourceManagerImpl.getInstance().getAPIResources(null, null, 1,
                APIResourceManagementConstants.SYSTEM_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getTotalCount();
        systemAPIs.addAll(APIResourceManagerImpl.getInstance().getAPIResources(null, null, systemAPICount,
                APIResourceManagementConstants.SYSTEM_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getAPIResources());
        // Get APIs with TENANT type.
        systemAPICount = APIResourceManagerImpl.getInstance().getAPIResources(null, null, 1,
                APIResourceManagementConstants.TENANT_ADMIN_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getTotalCount();
        systemAPIs.addAll(APIResourceManagerImpl.getInstance().getAPIResources(null, null, systemAPICount,
                APIResourceManagementConstants.TENANT_ADMIN_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getAPIResources());
        // Get APIs with TENANT type.
        systemAPICount = APIResourceManagerImpl.getInstance().getAPIResources(null, null, 1,
                APIResourceManagementConstants.TENANT_USER_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getTotalCount();
        systemAPIs.addAll(APIResourceManagerImpl.getInstance().getAPIResources(null, null, systemAPICount,
                APIResourceManagementConstants.TENANT_USER_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getAPIResources());
        // Get APIs with ORGANIZATION type.
        systemAPICount = APIResourceManagerImpl.getInstance().getAPIResources(null, null, 1,
                APIResourceManagementConstants.ORGANIZATION_ADMIN_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getTotalCount();
        systemAPIs.addAll(APIResourceManagerImpl.getInstance().getAPIResources(null, null, systemAPICount,
                APIResourceManagementConstants.ORGANIZATION_ADMIN_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getAPIResources());
        // Get APIs with ORGANIZATION type.
        systemAPICount = APIResourceManagerImpl.getInstance().getAPIResources(null, null, 1,
                APIResourceManagementConstants.ORGANIZATION_USER_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getTotalCount();
        systemAPIs.addAll(APIResourceManagerImpl.getInstance().getAPIResources(null, null, systemAPICount,
                APIResourceManagementConstants.ORGANIZATION_USER_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getAPIResources());
        // Get APIs with ME type.
        systemAPICount = APIResourceManagerImpl.getInstance().getAPIResources(null, null, 1,
                APIResourceManagementConstants.OTHER_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getTotalCount();
        systemAPIs.addAll(APIResourceManagerImpl.getInstance().getAPIResources(null, null, systemAPICount,
                APIResourceManagementConstants.OTHER_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getAPIResources());
        // Get APIs with CONSOLE_FEATURE type.
        systemAPICount = APIResourceManagerImpl.getInstance().getAPIResources(null, null, 1,
                APIResourceManagementConstants.CONSOLE_FEATURE_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getTotalCount();
        systemAPIs.addAll(APIResourceManagerImpl.getInstance().getAPIResources(null, null, systemAPICount,
                APIResourceManagementConstants.CONSOLE_FEATURE_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getAPIResources());
        return systemAPIs;
    }

    /**
     * Check whether the system APIs are registered in the tenant.
     *
     * @param tenantDomain tenant domain.
     * @return true if system APIs are not registered in the tenant.
     * @throws APIResourceMgtException if an error occurs while checking the existence of system APIs.
     */
    public static boolean isSystemAPIExist(String tenantDomain) throws APIResourceMgtException {

        return !APIResourceManagerImpl.getInstance()
                .getAPIResources(null, null, 1, APIResourceManagementConstants.TENANT_ADMIN_API_FILTER,
                        APIResourceManagementConstants.ASC, tenantDomain).getAPIResources().isEmpty();
    }

    public static boolean isSystemAPI(String type) {

        return !"BUSINESS".equalsIgnoreCase(type);
    }

    public Object getTenantId(String tenantDomain) {

        if (tenantDomain == null) {
            return null;
        }
        return IdentityTenantUtil.getTenantId(tenantDomain);
    }
}

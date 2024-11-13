/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManagerImpl;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtClientException;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtServerException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            Map<String, APIResource> duplicateConfigs = APIResourceManagementConfigBuilder.getInstance()
                    .getDuplicateAPIResourceConfigs();
            if (!isSystemAPIExist(tenantDomain)) {
                LOG.debug("Registering system API resources in the server.");
                registerAPIResources(new ArrayList<>(configs.values()), tenantDomain);
            } else {
                LOG.debug("System APIs are already registered in the server. Applying the latest configurations.");
                // Remove the existing system APIs from the configs.
                // Existing system APIs will be evaluated using the identifier.
                HashMap<String, APIResource> tempConfigs = new HashMap<>(configs);
                List<APIResource> systemAPIs = getSystemAPIs(tenantDomain);
                for (APIResource systemAPI : systemAPIs) {
                    tempConfigs.remove(systemAPI.getIdentifier());
                }
                // Register the new system APIs.
                registerAPIResources(new ArrayList<>(tempConfigs.values()), tenantDomain);
                // Handle duplicate system APIs.
                for (APIResource oldAPIResource : duplicateConfigs.values()) {
                    // Get the existing API resource from the DB.
                    APIResource apiResourceFromDB = APIResourceManagerImpl.getInstance().getAPIResourceByIdentifier(
                            oldAPIResource.getIdentifier(), tenantDomain);
                    // Get the updated API resource from the configs.
                    APIResource updatedAPIResource = configs.get(oldAPIResource.getIdentifier());
                    // Get the scopes which are not in the existing API resource.
                    List<Scope> addedScopes = updatedAPIResource.getScopes().stream()
                            .filter(scope1 -> apiResourceFromDB.getScopes().stream()
                                    .noneMatch(scope2 -> scope2.getName().equals(scope1.getName())))
                            .collect(Collectors.toList());
                    if (addedScopes.isEmpty() &&
                            StringUtils.equals(apiResourceFromDB.getType(), updatedAPIResource.getType())) {
                        continue;
                    }

                    APIResource updatedAPIResourceFromDB = new APIResource.APIResourceBuilder()
                            .id(apiResourceFromDB.getId())
                            .name(apiResourceFromDB.getName())
                            .description(apiResourceFromDB.getDescription())
                            .identifier(apiResourceFromDB.getIdentifier())
                            // Set the type as the updated API resource type.
                            .type(updatedAPIResource.getType())
                            .tenantId(apiResourceFromDB.getTenantId())
                            .requiresAuthorization(apiResourceFromDB.isAuthorizationRequired())
                            .scopes(apiResourceFromDB.getScopes())
                            .subscribedApplications(apiResourceFromDB.getSubscribedApplications())
                            .properties(apiResourceFromDB.getProperties())
                            .build();

                    // If there are scopes which are not in the existing API resource, update the API resource.
                    APIResourceManagerImpl.getInstance().updateAPIResource(updatedAPIResourceFromDB, addedScopes,
                            new ArrayList<>(), tenantDomain);
                }
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

        // Get APIs with SYSTEM type.
        int systemAPICount = APIResourceManagerImpl.getInstance().getAPIResources(null, null, 1,
                APIResourceManagementConstants.NON_BUSINESS_API_FILTER, APIResourceManagementConstants.ASC,
                tenantDomain).getTotalCount();
        return new ArrayList<>(APIResourceManagerImpl.getInstance().getAPIResources(null, null, systemAPICount,
                        APIResourceManagementConstants.NON_BUSINESS_API_FILTER, APIResourceManagementConstants.ASC,
                        tenantDomain).getAPIResources());
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
                .getAPIResources(null, null, 1, APIResourceManagementConstants.TENANT_API_FILTER,
                        APIResourceManagementConstants.ASC, tenantDomain).getAPIResources().isEmpty();
    }

    public static boolean isSystemAPI(String type) {

        return !APIResourceManagementConstants.BUSINESS_TYPE.equalsIgnoreCase(type)
                && !APIResourceManagementConstants.SYSTEM_TYPE.equalsIgnoreCase(type);
    }

    public static boolean isSystemAPIByAPIId(String apiId) throws APIResourceMgtException {

        APIResource apiResource = APIResourceManagerImpl.getInstance().getAPIResourceById(apiId,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        return apiResource != null && isSystemAPI(apiResource.getType());
    }

    public Object getTenantId(String tenantDomain) {

        if (tenantDomain == null) {
            return null;
        }
        return IdentityTenantUtil.getTenantId(tenantDomain);
    }
}

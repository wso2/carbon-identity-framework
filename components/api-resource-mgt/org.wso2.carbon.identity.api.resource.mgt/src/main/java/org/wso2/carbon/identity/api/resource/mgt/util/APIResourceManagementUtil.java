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
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

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
     *
     * @param tenantDomain tenant domain.
     */
    public static void addSystemAPIs(String tenantDomain) {

        LOG.debug("Registering System APIs in tenant domain: " + tenantDomain);
        Map<String, APIResource> configs = APIResourceManagementConfigBuilder.getInstance()
                .getAPIResourceMgtConfigurations();
        for (APIResource apiResource : configs.values()) {
            // Skip registering tenant management API in non-super tenant domains.
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)
                    && APIResourceManagementConstants.TENANT_MGT_API_NAME.equalsIgnoreCase(apiResource.getName())) {
                continue;
            }
            if (apiResource != null) {
                try {
                    APIResourceManagerImpl.getInstance().addAPIResource(apiResource, tenantDomain);
                } catch (APIResourceMgtException e) {
                    LOG.error("Error while registering system API resources in the tenant: " + tenantDomain);
                }
            }
        }
        LOG.debug("System APIs successfully registered in tenant domain: " + tenantDomain);
    }
}

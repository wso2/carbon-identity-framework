/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementConfigBuilder;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;

@WithH2Database(files = {"dbscripts/h2.sql"})

public class APIResourceManagementUtilTest {

    private APIResourceManager apiResourceManager;
    private static final String ORG_LEVEL_SERVER_CONFIG_API = "/o/api/server/v1/configs";
    private static final String ORGANIZATION_LEVEL_API = "ORGANIZATION";
    private static final String CONSOLE_ORG_FEATURE_API = "CONSOLE_ORG_LEVEL";

    @BeforeClass
    public void setUp() throws IdentityEventException {

        apiResourceManager = APIResourceManagerImpl.getInstance();

        // Trigger API resource configuration loading.
        APIResourceManagementConfigBuilder.getInstance();

        // Trigger System API register function on server startup.
        APIResourceManagementUtil.addSystemAPIs();
    }

    @Test
    public void testUpdateAPIResourceTypeFromSystemAPIResourceFile() throws APIResourceMgtException {

        /* Fetch already registered API which having duplicated configs in the system-api-resource.xml to mimic the
            flow of API resource type update by adding deployment.toml config. */
        APIResource apiResource = apiResourceManager.getAPIResourceByIdentifier(ORG_LEVEL_SERVER_CONFIG_API,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

        // Trigger System API register function on server startup. No API resource update yet.
        APIResourceManagementUtil.addSystemAPIs();

        // Update the API resource type to CONSOLE_ORG_LEVEL to test API resource type change flow.
        APIResource updatedAPIResource = new APIResource.APIResourceBuilder()
                .id(apiResource.getId())
                .name(apiResource.getName())
                .description(apiResource.getDescription())
                .identifier(apiResource.getIdentifier())
                .type(CONSOLE_ORG_FEATURE_API)
                .tenantId(apiResource.getTenantId())
                .requiresAuthorization(apiResource.isAuthorizationRequired())
                .scopes(apiResource.getScopes())
                .subscribedApplications(apiResource.getSubscribedApplications())
                .properties(apiResource.getProperties())
                .build();
        apiResourceManager.updateAPIResource(updatedAPIResource, new ArrayList<>(), new ArrayList<>(),
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

        /* Trigger System API register function on server startup. The system-api-resource.xml file contains the API
        resource type as ORGANIZATION which will initiates API resource type change flow as required in this test. */
        APIResourceManagementUtil.addSystemAPIs();

        apiResource = apiResourceManager.getAPIResourceByIdentifier(ORG_LEVEL_SERVER_CONFIG_API,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Assert.assertEquals(ORGANIZATION_LEVEL_API, apiResource.getType());
    }
}

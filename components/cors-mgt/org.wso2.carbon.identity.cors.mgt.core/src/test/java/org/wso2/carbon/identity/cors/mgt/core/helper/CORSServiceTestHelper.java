/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_ORIGIN_RESOURCE_NAME;
import static org.wso2.carbon.identity.cors.mgt.core.internal.Constants.CORS_ORIGIN_RESOURCE_TYPE_NAME;

/**
 * Helper class for CORSServiceTest.
 */
public class CORSServiceTestHelper {

    private static final Log log = LogFactory.getLog(CORSServiceTestHelper.class);

    public static void mockCarbonContextForTenant(int tenantId, String tenantDomain) {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);

        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomain);
        when(privilegedCarbonContext.getTenantId()).thenReturn(tenantId);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    public static void mockIdentityTenantUtility() {

        mockStatic(IdentityTenantUtil.class);
        IdentityTenantUtil identityTenantUtil = mock(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantDomain(any(Integer.class))).thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }

    public static ResourceTypeAdd getSampleResourceTypeAdd() {

        ResourceTypeAdd resourceTypeAdd = new ResourceTypeAdd();
        resourceTypeAdd.setName(CORS_ORIGIN_RESOURCE_TYPE_NAME);

        return resourceTypeAdd;
    }

    public static ResourceAdd getSampleResourceAdd(List<CORSOrigin> corsOrigins) {

        ObjectMapper mapper = new ObjectMapper();

        ResourceAdd resourceAdd = new ResourceAdd();
        resourceAdd.setName(CORS_ORIGIN_RESOURCE_NAME);
        List<Attribute> attributeList = new ArrayList<>();
        for (CORSOrigin corsOrigin : corsOrigins) {
            Attribute attribute = new Attribute();
            attribute.setKey(String.valueOf(corsOrigin.hashCode()));
            try {
                String corsOriginString = mapper.writeValueAsString(corsOrigin);
                attribute.setValue(corsOriginString);
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error while converting a CORS Origin to a String.", e);
                }
            }
            attributeList.add(attribute);
        }
        resourceAdd.setAttributes(attributeList);

        return resourceAdd;
    }

    public static CORSOrigin attributeToCORSOrigin(Attribute attribute) {

        ObjectMapper mapper = new ObjectMapper();

        CORSOrigin corsOrigin = null;
        try {
            corsOrigin = mapper.readValue(attribute.getValue(), CORSOrigin.class);
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while converting a String to a CORS Origin.", e);
            }
        }
        return corsOrigin;
    }
}

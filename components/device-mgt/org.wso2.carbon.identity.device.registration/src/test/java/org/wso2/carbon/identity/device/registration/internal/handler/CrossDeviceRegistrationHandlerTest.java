/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.registration.internal.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.registration.internal.component.DeviceRegistrationComponentServiceHolder;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the QR code data of CrossDeviceRegistrationHandler.
 */
public class CrossDeviceRegistrationHandlerTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String HOST_NAME = "localhost";
    private static final String ENDPOINT = "https://localhost:9443/t/test.com/api/server/v1/flow/execute";
    private static final String ONE_TIME_FLOW_ID = "one-time-flow-id";
    private static final String SUB_ORG_TENANT_DOMAIN = "sub-org-tenant";
    private static final String SUB_ORG_ID = "sub-org-id";

    private MockedStatic<ServiceURLBuilder> serviceURLBuilderMocked;
    private MockedStatic<IdentityUtil> identityUtilMocked;
    private MockedStatic<OrganizationManagementUtil> organizationManagementUtilMocked;
    private ServiceURLBuilder builder;
    private OrganizationManager organizationManager;
    private OrganizationManager originalOrganizationManager;

    @BeforeMethod
    public void setUp() throws Exception {

        builder = mock(ServiceURLBuilder.class);
        when(builder.addPath(anyString())).thenReturn(builder);
        when(builder.setTenant(anyString())).thenReturn(builder);
        when(builder.setOrganization(anyString())).thenReturn(builder);
        ServiceURL serviceURL = mock(ServiceURL.class);
        when(serviceURL.getAbsolutePublicURL()).thenReturn(ENDPOINT);
        when(builder.build(HOST_NAME)).thenReturn(serviceURL);
        serviceURLBuilderMocked = mockStatic(ServiceURLBuilder.class);
        serviceURLBuilderMocked.when(ServiceURLBuilder::create).thenReturn(builder);
        identityUtilMocked = mockStatic(IdentityUtil.class);
        identityUtilMocked.when(IdentityUtil::getHostName).thenReturn(HOST_NAME);
        organizationManagementUtilMocked = mockStatic(OrganizationManagementUtil.class);
        organizationManagementUtilMocked.when(() -> OrganizationManagementUtil.isOrganization(SUB_ORG_TENANT_DOMAIN))
                .thenReturn(true);
        organizationManager = mock(OrganizationManager.class);
        when(organizationManager.resolveOrganizationId(SUB_ORG_TENANT_DOMAIN)).thenReturn(SUB_ORG_ID);
        originalOrganizationManager = DeviceRegistrationComponentServiceHolder.getInstance().getOrganizationManager();
        DeviceRegistrationComponentServiceHolder.getInstance().setOrganizationManager(organizationManager);
    }

    @AfterMethod
    public void tearDown() {

        serviceURLBuilderMocked.close();
        identityUtilMocked.close();
        organizationManagementUtilMocked.close();
        DeviceRegistrationComponentServiceHolder.getInstance().setOrganizationManager(originalOrganizationManager);
    }

    @Test
    public void testQrCodeDataHasTheEndpointMethodAndFlowId() throws Exception {

        JsonNode qrCodeData = new ObjectMapper().readTree(
                CrossDeviceRegistrationHandler.buildQrCodeData(ONE_TIME_FLOW_ID, TENANT_DOMAIN));

        assertEquals(qrCodeData.get("endpoint").asText(), ENDPOINT);
        assertEquals(qrCodeData.get("method").asText(), "POST");
        assertEquals(qrCodeData.get("data").get("flowId").asText(), ONE_TIME_FLOW_ID);
        assertEquals(qrCodeData.size(), 3);
        verify(builder).addPath("/api/server/v1/flow/execute");
        verify(builder).setTenant(TENANT_DOMAIN);
        verify(builder, never()).setOrganization(anyString());
    }

    @Test
    public void testQrCodeDataOfSubOrganizationFlowUsesTheOrganizationPath() throws Exception {

        CrossDeviceRegistrationHandler.buildQrCodeData(ONE_TIME_FLOW_ID, SUB_ORG_TENANT_DOMAIN);

        verify(builder).setOrganization(SUB_ORG_ID);
        verify(builder, never()).setTenant(anyString());
    }
}

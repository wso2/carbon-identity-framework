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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.framework.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.framework.testutil.powermock.PowerMockIdentityBaseTest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.LOGOUT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TYPE;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * Unit tests for {@link DefaultRequestCoordinator}.
 */
@WithCarbonHome
@PrepareForTest(IdentityTenantUtil.class)
public class DefaultRequestCoordinatorTest extends PowerMockIdentityBaseTest {

    private DefaultRequestCoordinator requestCoordinator;

    @BeforeMethod
    public void setUp() throws Exception {

        requestCoordinator = new DefaultRequestCoordinator();
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @DataProvider(name = "tenantDomainProvider")
    public Object[][] provideTenantDomain() {

        return new Object[][]{
                {true, null, null, SUPER_TENANT_DOMAIN_NAME},
                {false, null, null, SUPER_TENANT_DOMAIN_NAME},

                {true, "foo.com", "xyz.com", "foo.com"},
                {false, "foo.com", "xyz.com", "xyz.com"},

                {true, null, "xyz.com", "xyz.com"},
                {false, null, "xyz.com", "xyz.com"},
        };
    }

    @Test(dataProvider = "tenantDomainProvider")
    public void testTenantDomainInAuthenticationContext(boolean isTenantQualifiedUrlModeEnabled,
                                                        String tenantDomainInThreadLocal,
                                                        String tenantDomainInRequestParam,
                                                        String expected) throws Exception {

        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.isTenantQualifiedUrlsEnabled()).thenReturn(isTenantQualifiedUrlModeEnabled);
        when(IdentityTenantUtil.getTenantDomainFromContext()).thenReturn(tenantDomainInThreadLocal);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TYPE)).thenReturn("oauth");
        when(request.getParameter(LOGOUT)).thenReturn("true");
        when(request.getParameter(TENANT_DOMAIN)).thenReturn(tenantDomainInRequestParam);

        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthenticationContext context = requestCoordinator.initializeFlow(request, response);

        assertEquals(context.getTenantDomain(), expected);
    }
}

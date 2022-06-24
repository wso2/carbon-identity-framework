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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.framework.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.framework.common.testng.WithH2Database;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

@Test
@WithH2Database(jndiName = "jdbc/WSO2CarbonDB", files = {"dbScripts/h2.sql"})
@WithCarbonHome
public class GraphBasedSequenceHandlerFailTest extends GraphBasedSequenceHandlerAbstractTest {

    @Test
    public void handleFailMethodWithParamsTest() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        ServiceProvider sp1 = getTestServiceProvider("js-sp-fail-method-with-params-onSuccess.xml");

        AuthenticationContext context = getAuthenticationContext(sp1);

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.<String, String[]>emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS)).thenReturn(AuthenticatorFlowStatus
                .SUCCESS_COMPLETED);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");
        graphBasedSequenceHandler.handle(req, resp, context);

        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertFalse(context.isRequestAuthenticated());
        assertEquals(context.getProperty(FrameworkConstants.AUTH_ERROR_CODE), "access_denied");
        assertEquals(context.getProperty(FrameworkConstants.AUTH_ERROR_MSG), "login could not be completed");
        assertEquals(context.getProperty(FrameworkConstants.AUTH_ERROR_URI), "https://wso2.com/");
    }

    @Test
    public void handleFailMethodWithoutParamsTest() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        ServiceProvider sp1 = getTestServiceProvider("js-sp-fail-method-without-params-onSuccsss.xml");

        AuthenticationContext context = getAuthenticationContext(sp1);

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.<String, String[]>emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS)).thenReturn(AuthenticatorFlowStatus
                .SUCCESS_COMPLETED);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertFalse(context.isRequestAuthenticated());
    }

    @Test
    public void handleFailMethodWithParamsOnFailTest() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        ServiceProvider sp1 = getTestServiceProvider("js-sp-fail-method-with-params-onFail.xml");

        AuthenticationContext context = getAuthenticationContext(sp1);

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.<String, String[]>emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS)).thenReturn(AuthenticatorFlowStatus
                .FAIL_COMPLETED);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");
        graphBasedSequenceHandler.handle(req, resp, context);

        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertFalse(context.isRequestAuthenticated());
        assertEquals(context.getProperty(FrameworkConstants.AUTH_ERROR_CODE), "access_denied");
        assertEquals(context.getProperty(FrameworkConstants.AUTH_ERROR_MSG), "login could not be completed");
        assertEquals(context.getProperty(FrameworkConstants.AUTH_ERROR_URI), "https://wso2.com/");
    }

    @Test
    public void handleFailMethodWithoutParamsOnFailTest() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        ServiceProvider sp1 = getTestServiceProvider("js-sp-fail-method-without-params-onFail.xml");

        AuthenticationContext context = getAuthenticationContext(sp1);

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.<String, String[]>emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS)).thenReturn(AuthenticatorFlowStatus
                .FAIL_COMPLETED);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertFalse(context.isRequestAuthenticated());
    }
}

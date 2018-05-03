/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.javascript.flow.HasRoleFunction;
import org.wso2.carbon.identity.application.authentication.framework.javascript.flow.IsExistsStringFunction;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Test
public class GraphBasedSequenceHandlerAcrTest extends GraphBasedSequenceHandlerAbstractTest {

    @Test(dataProvider = "staticAcrDataProvider")
    public void testHandle_Static_Javascript_Acr(String spFileName, String[] acrArray, int authHistoryCount) throws
            Exception {
        ServiceProvider sp1 = getTestServiceProvider(spFileName);

        AuthenticationContext context = getAuthenticationContext(sp1);
        if (acrArray != null) {
            for (String acr : acrArray) {
                context.addRequestedAcr(acr);
            }
        }

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.<String, String[]>emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(authHistories.size(), authHistoryCount);
    }

    @DataProvider(name = "staticAcrDataProvider")
    public Object[][] getStaticAcrRolesData() {
        return new Object[][]{
                {"js-sp-1.xml", new String[]{"acr1"}, 1},
                {"js-sp-1.xml", new String[]{"acr2"}, 2},
                {"js-sp-1.xml", new String[]{"notMatchingAcr"}, 3}
        };
    }

    @Test(dataProvider = "roleBasedAcrDataProvider")
    public void testHandle_RoleBased_Javascript_Acr(String spFileName, String[] acrArray, int authHistoryCount,
                                                    boolean hasRole) throws Exception {
        HasRoleFunction hasRoleFunction = mock(HasRoleFunction.class);
        when(hasRoleFunction.contains(any(JsAuthenticationContext.class), anyString())).thenReturn(hasRole);
        graphBuilderFactory.getJsFunctionRegistry().register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "hasRole",
                                (IsExistsStringFunction) hasRoleFunction::contains);
        ServiceProvider sp1 = getTestServiceProvider(spFileName);

        AuthenticationContext context = getAuthenticationContext(sp1);
        if (acrArray != null) {
            for (String acr : acrArray) {
                context.addRequestedAcr(acr);
            }
        }

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.<String, String[]>emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(authHistories.size(), authHistoryCount);
    }

    @DataProvider(name = "roleBasedAcrDataProvider")
    public Object[][] getRoleBasedAcrUserRolesData() {
        return new Object[][]{
                {"js-sp-2.xml", new String[]{"acr1"}, 1, false},
                {"js-sp-2.xml", new String[]{"acr2"}, 2, true}
        };
    }

    @Test(expectedExceptions = FrameworkException.class)
    public void testHandle_Incorrect_Javascript_Acr() throws Exception {
        ServiceProvider sp1 = getTestServiceProvider("incorrect-js-sp-1.xml");

        AuthenticationContext context = getAuthenticationContext(sp1);

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.<String, String[]>emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

    }

    @Test(expectedExceptions = FrameworkException.class)
    public void testHandle_Incorrect_Function_Javascript_Acr() throws Exception {
        ServiceProvider sp1 = getTestServiceProvider("incorrect-function-js-sp-1.xml");

        AuthenticationContext context = getAuthenticationContext(sp1);

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

    }
}
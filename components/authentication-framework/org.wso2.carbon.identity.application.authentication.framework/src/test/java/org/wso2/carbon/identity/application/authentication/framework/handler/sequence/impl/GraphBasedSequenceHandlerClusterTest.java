/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang3.SerializationUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.MockUiAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.javascript.flow.HasRoleFunction;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class GraphBasedSequenceHandlerClusterTest extends GraphBasedSequenceHandlerAbstractTest {

    @BeforeMethod
    @Override
    protected void setUp()
            throws UserStoreException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException {

        super.setUp();
        FrameworkServiceDataHolder.getInstance().getAuthenticators().clear();
        FrameworkServiceDataHolder.getInstance().getAuthenticators()
                .add(new MockUiAuthenticator("BasicMockAuthenticator", new MockSubjectCallback()));
        FrameworkServiceDataHolder.getInstance().getAuthenticators()
                .add(new MockUiAuthenticator("HwkMockAuthenticator", new MockSubjectCallback()));

    }

    @Test(dataProvider = "roleBasedDataProvider")
    public void testHandle_RequestHop(String spFileName, int authHistoryCount, boolean hasRole) throws Exception {

        HasRoleFunction hasRoleFunction = mock(HasRoleFunction.class);
        when(hasRoleFunction.contains(any(JsAuthenticationContext.class), anyString())).thenReturn(hasRole);

        ServiceProvider sp1 = getTestServiceProvider(spFileName);

        AuthenticationContext context = getClusterHoppingAuthenticationContext(sp1);

        Map<String, Object> attributesMap = new HashMap<>();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getAttribute(anyString())).then(m -> attributesMap.get(m.getArguments()[0]));
        doAnswer(m -> attributesMap.put(((String) m.getArguments()[0]), m.getArguments()[1])).when(req)
                .setAttribute(anyString(), anyObject());

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler = new GraphBasedSequenceHandler();
        graphBasedSequenceHandler.handle(req, resp, context);

        //Simulate the request goes to next node
        graphBasedSequenceHandler = new GraphBasedSequenceHandler();
        AuthenticationContext deseralizedContext = (AuthenticationContext) SerializationUtils
                .deserialize(SerializationUtils.serialize(context));
        when(req.getParameter("returning")).thenReturn("true");
        graphBasedSequenceHandler.handle(req, resp, deseralizedContext);

        List<AuthHistory> authHistories = deseralizedContext.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(authHistories.size(), 1);

        when(req.getParameter("returning")).thenReturn("true");
        when(req.getAttribute(FrameworkConstants.REQ_ATTR_HANDLED)).thenReturn(false);

        //Simulate the request goes to another node
        graphBasedSequenceHandler = new GraphBasedSequenceHandler();
        deseralizedContext = (AuthenticationContext) SerializationUtils
                .deserialize(SerializationUtils.serialize(deseralizedContext));
        graphBasedSequenceHandler.handle(req, resp, deseralizedContext);

        authHistories = deseralizedContext.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(authHistories.size(), authHistoryCount);
    }

    private AuthenticationContext getClusterHoppingAuthenticationContext(ServiceProvider sp1)
            throws FrameworkException {

        AuthenticationContext context = getAuthenticationContext(sp1);

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.<String, String[]>emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);
        return context;
    }

    @DataProvider(name = "roleBasedDataProvider")
    public Object[][] getRoleBasedUserRolesData() {

        return new Object[][] { { "js-sp-3.xml", 2, true } };
    }
}

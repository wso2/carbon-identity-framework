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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistrar;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsFunctionRegistrarImpl;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import static org.mockito.Mockito.mock;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class GraphBasedSequenceHandlerCustomFunctionsTest extends GraphBasedSequenceHandlerAbstractTest {

    public void testHandle_Dynamic_Javascript_1() throws Exception {

        JsFunctionRegistrarImpl jsFunctionRegistrar = new JsFunctionRegistrarImpl();
        configurationLoader.setJsFunctionRegistrar(jsFunctionRegistrar);
        jsFunctionRegistrar.register(JsFunctionRegistrar.Subsystem.SEQUENCE_HANDLER, "fn1",
                (Function<AuthenticationContext, String>) this::customFunction1);
        jsFunctionRegistrar.register(JsFunctionRegistrar.Subsystem.SEQUENCE_HANDLER, "fn2",
                new CustomFunctionImpl2());

        AuthenticationContext context = getAuthenticationContextAcrStatic(new String[] { "acr1" });
        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(3, authHistories.size());
    }

    private AuthenticationContext getAuthenticationContextAcrStatic(String[] acrArray)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, FrameworkException,
            XMLStreamException {
        ServiceProvider sp1 = getTestServiceProvider("js-sp-dynamic-1.xml");

        AuthenticationContext context = getAuthenticationContext("", APPLICATION_AUTHENTICATION_FILE_NAME, sp1);
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
        return context;
    }

    public String customFunction1(AuthenticationContext context) {
        return "testResult1";
    }

    @FunctionalInterface
    public interface CustomFunctionInterface2 {

        String customFunction2(AuthenticationContext context, String param1, String param2);
    }

    public class CustomFunctionImpl2 implements CustomFunctionInterface2 {

        public String customFunction2(AuthenticationContext context, String param1, String param2) {
            return "testResult2";
        }
    }
}
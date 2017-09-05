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

import org.apache.commons.lang3.SerializationUtils;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsFunctionRegistryImpl;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.javascript.flow.IsExistsStringFunction;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Test
public class GraphBasedSequenceHandlerCustomFunctionsTest extends GraphBasedSequenceHandlerAbstractTest {

    @Test
    public void testHandle_Dynamic_Javascript_1() throws Exception {

        JsFunctionRegistryImpl jsFunctionRegistrar = new JsFunctionRegistryImpl();
        configurationLoader.setJsFunctionRegistrar(jsFunctionRegistrar);
        jsFunctionRegistrar.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "fn1",
                (Function<AuthenticationContext, String>) GraphBasedSequenceHandlerCustomFunctionsTest::customFunction1);
        jsFunctionRegistrar.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "fn2", new CustomFunctionImpl2());

        AuthenticationContext context = processSequenceWithAcr(new String[] { "acr1" });
        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(3, authHistories.size());
        assertEquals(authHistories.get(0).getAuthenticatorName(), "BasicMockAuthenticator");
        assertEquals(authHistories.get(1).getAuthenticatorName(), "HwkMockAuthenticator");
        assertEquals(authHistories.get(2).getAuthenticatorName(), "FptMockAuthenticator");
    }

    public void testHandle_Dynamic_Boolean() throws Exception {

        JsFunctionRegistryImpl jsFunctionRegistrar = new JsFunctionRegistryImpl();
        configurationLoader.setJsFunctionRegistrar(jsFunctionRegistrar);
        jsFunctionRegistrar.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "fn1",
                (Function<AuthenticationContext, String>) GraphBasedSequenceHandlerCustomFunctionsTest::customFunction1);
        jsFunctionRegistrar.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "getTrueFunction",
                (Function<AuthenticationContext, Boolean>) GraphBasedSequenceHandlerCustomFunctionsTest::customBoolean);

        jsFunctionRegistrar.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "getTrueFunction2",
                (IsExistsStringFunction) GraphBasedSequenceHandlerCustomFunctionsTest::customBoolean2);

        ServiceProvider sp1 = getTestServiceProvider("js-sp-dynamic-1.xml");

        String script =
                "function(context) {\n" + "    var myBool = getTrueFunction2(context, 'a');\n"
                        + "    Log.info(\"My Bool Value \"+myBool);\n" + "    if(myBool) {\n"
                        + "        Log.info(\"My Bool Is Selected \"+myBool);\n" + "        executeStep({id :'1',\n"
                        + "        on : {\n" + "            success : function(context) {executeStep({id :'3'});}\n"
                        + "        }});\n" + "        executeStep({id :'2'});\n" + "    }  else {\n"
                        + "        Log.info(\"My Bool Not Selected \"+myBool);\n" + "        executeStep({id :'1'});\n"
                        + "        executeStep({id :'3'});\n" + "    }\n" + "}\n";
        sp1.getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig().setContent(script);

        AuthenticationContext context = processAndGetAuthenticationContext(new String[0], sp1);
        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals( authHistories.size(), 3);
        assertEquals(authHistories.get(0).getAuthenticatorName(), "BasicMockAuthenticator");
        assertEquals(authHistories.get(1).getAuthenticatorName(), "FptMockAuthenticator");
        assertEquals(authHistories.get(2).getAuthenticatorName(), "HwkMockAuthenticator");
    }

    @Test
    public void testHandle_Dynamic_Javascript_Serialization() throws Exception {

        JsFunctionRegistryImpl jsFunctionRegistrar = new JsFunctionRegistryImpl();
        configurationLoader.setJsFunctionRegistrar(jsFunctionRegistrar);
        jsFunctionRegistrar.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "fn1",
                (Function<AuthenticationContext, String>) GraphBasedSequenceHandlerCustomFunctionsTest::customFunction1);

        ServiceProvider sp1 = getTestServiceProvider("js-sp-dynamic-1.xml");

        AuthenticationContext context = getAuthenticationContext("", APPLICATION_AUTHENTICATION_FILE_NAME, sp1);

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.<String, String[]>emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        byte[] serialized = SerializationUtils.serialize(context);

        AuthenticationContext deseralizedContext = (AuthenticationContext) SerializationUtils.deserialize(serialized);
        assertNotNull(deseralizedContext);

        HttpServletRequest req = mock(HttpServletRequest.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, deseralizedContext);

        List<AuthHistory> authHistories = deseralizedContext.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(3, authHistories.size());
        assertEquals(authHistories.get(0).getAuthenticatorName(), "BasicMockAuthenticator");
        assertEquals(authHistories.get(1).getAuthenticatorName(), "HwkMockAuthenticator");
        assertEquals(authHistories.get(2).getAuthenticatorName(), "FptMockAuthenticator");
    }

    private AuthenticationContext processSequenceWithAcr(String[] acrArray)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, FrameworkException,
            XMLStreamException {
        ServiceProvider sp1 = getTestServiceProvider("js-sp-dynamic-1.xml");

        return processAndGetAuthenticationContext(acrArray, sp1);
    }

    private AuthenticationContext processAndGetAuthenticationContext(String[] acrArray, ServiceProvider sp1)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, FrameworkException {
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

    public static String customFunction1(AuthenticationContext context) {
        return "testResult1";
    }

    public static Boolean customBoolean(AuthenticationContext context) {
        return true;
    }

    public static Boolean customBoolean2(AuthenticationContext context, String value) {
        return true;
    }

    @FunctionalInterface
    public interface CustomFunctionInterface2 extends Serializable {

        String customFunction2(AuthenticationContext context, String param1, String param2);
    }

    public class CustomFunctionImpl2 implements CustomFunctionInterface2 {

        public String customFunction2(AuthenticationContext context, String param1, String param2) {
            return "testResult2";
        }
    }
}
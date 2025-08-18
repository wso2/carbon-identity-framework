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

import org.graalvm.polyglot.HostAccess;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsFunctionRegistryImpl;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.CacheBackedLongWaitStatusDAO;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.LongWaitStatusDAOImpl;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.store.LongWaitStatusStoreService;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.central.log.mgt.internal.CentralLogMgtServiceComponentHolder;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestAttribute.HTTP_REQUEST;

@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbScripts/h2.sql"})
@WithCarbonHome
@WithRealmService(injectToSingletons =
        {IdentityCoreServiceDataHolder.class, FrameworkServiceDataHolder.class})
public class GraphBasedSequenceHandlerLongWaitTest extends GraphBasedSequenceHandlerAbstractTest {

    @BeforeClass
    public void setUpMocks() {

        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = true;
        IdentityEventService identityEventService = mock(IdentityEventService.class);
        CentralLogMgtServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
    }

    @AfterClass
    public void tearDown() {

        CentralLogMgtServiceComponentHolder.getInstance().setIdentityEventService(null);
    }

    @Test
    public void testHandleLongWait() throws Exception {

        LoggerUtils.isLogMaskingEnable = false;
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        JsFunctionRegistryImpl jsFunctionRegistrar = new JsFunctionRegistryImpl();
        FrameworkServiceDataHolder.getInstance().setJsFunctionRegistry(jsFunctionRegistrar);
        LongWaitStatusDAOImpl daoImpl = new LongWaitStatusDAOImpl();
        CacheBackedLongWaitStatusDAO cacheBackedDao = new CacheBackedLongWaitStatusDAO(daoImpl);
        FrameworkServiceDataHolder.getInstance().setLongWaitStatusStoreService(new LongWaitStatusStoreService
                (cacheBackedDao, 5000));
        jsFunctionRegistrar.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "testLongWaitCall",
                new AsyncAnalyticsCbFunctionImpl());

        ServiceProvider sp1 = getTestServiceProvider("js-sp-longwait-1.xml");
        AuthenticationContext context = getAuthenticationContext(sp1);
        context.setSessionIdentifier("1234");
        FrameworkServiceDataHolder.getInstance().setAdaptiveAuthenticationAvailable(true);
        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = createMockHttpServletRequest();
        context.setProperty(HTTP_REQUEST, new TransientObjectWrapper<>(req));

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

        assertTrue(context.isRequestAuthenticated());
    }

    @FunctionalInterface
    public interface Fn1 {

        void publishEvent(String siddhiAppName, String inStreamName, String outStreamName,
                          Map<String, Object> payloadData, Map<String, Object> eventHandlers);
    }

    public static class AsyncAnalyticsCbFunctionImpl implements Fn1 {

        @HostAccess.Export
        public void publishEvent(String siddhiAppName, String inStreamName, String outStreamName,
                                 Map<String, Object> payloadData, Map<String, Object> eventHandlers) {

            Map<String, Object> propMap = new HashMap<>();

            Map<String, String> nestedMap1 = new HashMap<>();
            nestedMap1.put("key1", "value1");
            nestedMap1.put("key2", "value2");

            Map<String, String> nestedMap2 = new HashMap<>();
            nestedMap2.put("key3", "value3");
            nestedMap2.put("key4", "value4");

            Object[] arrayElement = new Object[2];
            arrayElement[0] = nestedMap1;
            arrayElement[1] = "arrayString";

            List<Object> listElement = new ArrayList<>();
            listElement.add(nestedMap2);
            listElement.add("listString");

            propMap.put("arrayKey", arrayElement);
            propMap.put("listKey", listElement);

            AsyncProcess asyncProcess = new AsyncProcess((ctx, r) -> {
                r.accept(ctx, propMap, "onSuccess");
            });
            JsGraphBuilder.addLongWaitProcess(asyncProcess, eventHandlers);
        }
    }

    protected HttpServletRequest createMockHttpServletRequest() {

        HttpServletRequest req = mock(HttpServletRequest.class);
        Map<String, Object> attributes = new HashMap<>();
        doAnswer(m -> attributes.put(m.getArgument(0), m.getArgument(1))).when(req)
                .setAttribute(anyString(), any());

        doAnswer(m -> attributes.get(m.getArgument(0))).when(req).getAttribute(anyString());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stringAttribute", "stringAttributeValue");
        parameters.put("arrayAttribute", new String[]{"arrayValue1", "arrayValue2"});

        doAnswer(m -> parameters).when(req).getParameterMap();

        return req;
    }
}

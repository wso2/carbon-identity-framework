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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsFunctionRegistryImpl;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.store.LongWaitStatusStoreService;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;

@Test
public class GraphBasedSequenceHandlerLongWaitTest extends GraphBasedSequenceHandlerAbstractTest {

    @Test
    public void testHandleLongWait() throws Exception {

        JsFunctionRegistryImpl jsFunctionRegistrar = new JsFunctionRegistryImpl();
        FrameworkServiceDataHolder.getInstance().setJsFunctionRegistry(jsFunctionRegistrar);
        FrameworkServiceDataHolder.getInstance().setLongWaitStatusStoreService(new LongWaitStatusStoreService());
        jsFunctionRegistrar.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "testLongWaitCall",
                new AsyncAnalyticsCbFunctionImpl());

        ServiceProvider sp1 = getTestServiceProvider("js-sp-longwait-1.xml");
        AuthenticationContext context = getAuthenticationContext(sp1);
        context.setSessionIdentifier("1234");
        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = createMockHttpServletRequest();

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

    }

    @FunctionalInterface
    public interface Fn1 {

        void publishEvent(String siddhiAppName, String inStreamName, String outStreamName,
                          Map<String, Object> payloadData, Map<String, Object> eventHandlers);
    }

    public static class AsyncAnalyticsCbFunctionImpl implements Fn1 {

        public void publishEvent(String siddhiAppName, String inStreamName, String outStreamName,
                                 Map<String, Object> payloadData, Map<String, Object> eventHandlers) {

            AsyncProcess asyncProcess = new AsyncProcess((ctx, r) -> {
                System.out.println("Calling With : " + ctx);
                r.accept(ctx, Collections.emptyMap(), "onSuccess");
            });
            JsGraphBuilder.addLongWaitProcess(asyncProcess, eventHandlers);
        }
    }
}

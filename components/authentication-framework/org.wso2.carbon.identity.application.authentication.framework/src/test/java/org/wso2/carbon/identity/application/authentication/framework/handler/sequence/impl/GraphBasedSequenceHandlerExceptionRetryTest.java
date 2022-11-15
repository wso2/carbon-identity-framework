/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.MockAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsFunctionRegistryImpl;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn.JsNashornAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.CacheBackedLongWaitStatusDAO;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.LongWaitStatusDAOImpl;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.store.LongWaitStatusStoreService;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;

@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbScripts/h2.sql"})
@WithCarbonHome
@WithRealmService(injectToSingletons =
        {IdentityCoreServiceDataHolder.class, FrameworkServiceDataHolder.class})
@WithRegistry(injectToSingletons = {FrameworkServiceDataHolder.class})
public class GraphBasedSequenceHandlerExceptionRetryTest extends GraphBasedSequenceHandlerAbstractTest {

    private static final String CONTEXT_ATTRIBUTE_NAME_CURRENT_FAIL_TRIES = "RetriesOnTest";

    public void testExceptionRetry() throws Exception {

        LoggerUtils.isLogMaskingEnable = false;
        JsFunctionRegistryImpl jsFunctionRegistrar = new JsFunctionRegistryImpl();
        FrameworkServiceDataHolder.getInstance().setJsFunctionRegistry(jsFunctionRegistrar);
        LongWaitStatusDAOImpl daoImpl = new LongWaitStatusDAOImpl();
        CacheBackedLongWaitStatusDAO cacheBackedDao = new CacheBackedLongWaitStatusDAO(daoImpl);

        FrameworkServiceDataHolder.getInstance().getAuthenticators().add(
                new FailingMockAuthenticator("FailingMockAuthenticator"));

        FrameworkServiceDataHolder.getInstance().setLongWaitStatusStoreService(new LongWaitStatusStoreService
                (cacheBackedDao, 5000));
        jsFunctionRegistrar.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "hasAnyOfTheRoles",
                (BiFunction<JsNashornAuthenticatedUser, List<String>, Boolean>) this::hasAnyOfTheRolesFunction);

        ServiceProvider sp1 = getTestServiceProvider("js-sp-exception-retry.xml");
        AuthenticationContext context = getAuthenticationContext(sp1);
        context.setSessionIdentifier("1234");
        FrameworkServiceDataHolder.getInstance().setAdaptiveAuthenticationAvailable(true);
        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = createMockHttpServletRequest();

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

        Integer currentAttempts = (Integer) context.getProperties().get(CONTEXT_ATTRIBUTE_NAME_CURRENT_FAIL_TRIES);

        Assert.assertNotNull(currentAttempts);
        Assert.assertEquals(currentAttempts.intValue(), 2);
    }

    public boolean hasAnyOfTheRolesFunction(JsNashornAuthenticatedUser jsAuthenticatedUser, List<String> args) {

        return args.stream().anyMatch(s -> s.contains("Role1"));
    }

    /**
     * Authenticator which used to fail a given number of tries.
     * This is to simulate the tetry mechanism with exception.
     */
    private class FailingMockAuthenticator extends MockAuthenticator {

        private int failTries = 1;

        public FailingMockAuthenticator(String name) {

            super(name);
        }

        @Override
        public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
                                               AuthenticationContext context)
                throws AuthenticationFailedException, LogoutFailedException {

            Integer currentAttempts = (Integer) context.getProperties().get(CONTEXT_ATTRIBUTE_NAME_CURRENT_FAIL_TRIES);
            if (currentAttempts == null) {
                currentAttempts = 0;
            }
            Integer newAttempts = currentAttempts + 1;
            context.getProperties().put(CONTEXT_ATTRIBUTE_NAME_CURRENT_FAIL_TRIES, newAttempts);

            if (currentAttempts < failTries) {
                throw new AuthenticationFailedException(
                        "Simulating an authentication error at attempt : " + currentAttempts);
            } else {
                return super.process(request, response, context);
            }
        }
    }

}

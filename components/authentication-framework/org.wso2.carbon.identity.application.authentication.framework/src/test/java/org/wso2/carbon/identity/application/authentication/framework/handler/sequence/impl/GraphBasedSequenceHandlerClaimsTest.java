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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.framework.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.framework.common.testng.WithH2Database;
import org.wso2.carbon.identity.framework.common.testng.WithRealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;

/**
 * Tests the claims in the Javascript.
 */
@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbScripts/h2.sql"})
@WithCarbonHome
@WithRealmService(injectToSingletons =
        {IdentityCoreServiceDataHolder.class, FrameworkServiceDataHolder.class})
public class GraphBasedSequenceHandlerClaimsTest extends GraphBasedSequenceHandlerAbstractTest {

    public void testHandleClaimHandling() throws Exception {

        ServiceProvider sp1 = getTestServiceProvider("js-sp-5-claim.xml");

        AuthenticationContext context = getAuthenticationContext(sp1);

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = createMockHttpServletRequest();

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

        Assert.assertEquals(context.getRuntimeClaim("http://wso2.org/custom/claim1"), "value1");
        Assert.assertEquals(context.getRuntimeClaim("http://wso2.org/claims/lastname"), "newLastNameValue");
    }
}

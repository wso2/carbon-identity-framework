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

import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JSExecutionSupervisor;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the claims in the Javascript.
 */
@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbScripts/h2.sql"})
@WithRealmService
public class GraphBasedSequenceHandlerClaimsTest extends GraphBasedSequenceHandlerAbstractTest {

    @BeforeTest
    public void init() {

        JSExecutionSupervisor jsExecutionSupervisor = new JSExecutionSupervisor(1, 5000L);
        FrameworkServiceDataHolder.getInstance().setJsExecutionSupervisor(jsExecutionSupervisor);
    }

    @AfterTest
    public void teardown() {

        FrameworkServiceDataHolder.getInstance().getJsExecutionSupervisor().shutdown();
    }

    public void testHandleClaimHandling() throws Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        ServiceProvider sp1 = getTestServiceProvider("js-sp-5-claim.xml");

        AuthenticationContext context = getAuthenticationContext(sp1);

        SequenceConfig sequenceConfig = configurationLoader
            .getSequenceConfig(context, Collections.emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = createMockHttpServletRequest();

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        RealmService mockRealmService = mock(RealmService.class);
        UserRealm mockUserRealm = mock(UserRealm.class);
        AbstractUserStoreManager mockUserStoreManager = PowerMockito.mock(AbstractUserStoreManager.class);
        when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);
        FrameworkServiceDataHolder.getInstance().setRealmService(mockRealmService);
        PowerMockito.when(mockUserStoreManager.getUserClaimValues(anyString(), eq(new String[]{"http://wso2.org/claims/lastname"})
            , anyString())).thenReturn(Collections.singletonMap("http://wso2.org/claims/lastname", "lastNameValue"));

        graphBasedSequenceHandler.handle(req, resp, context);

        Assert.assertEquals(context.getRuntimeClaim("http://wso2.org/custom/claim1"), "value1");
        Assert.assertEquals(context.getRuntimeClaim("http://wso2.org/claims/lastname"), "newLastNameValue");
    }
}

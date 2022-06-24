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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.framework.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.framework.common.testng.WithH2Database;
import org.wso2.carbon.identity.framework.common.testng.WithRealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbScripts/h2.sql"})
@WithCarbonHome
@WithRealmService(injectToSingletons =
        {IdentityCoreServiceDataHolder.class, FrameworkServiceDataHolder.class})
public class GraphBasedSequenceHandlerNoJsTest extends GraphBasedSequenceHandlerAbstractTest {

    @Test(dataProvider = "noJsDataProvider")
    public void testHandleStaticSequence(String spFileName, int authHistoryCount) throws
            Exception {

        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        ServiceProvider sp1 = getTestServiceProvider(spFileName);

        AuthenticationContext context = getAuthenticationContext(sp1);

        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = mock(HttpServletRequest.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        UserCoreUtil.setDomainInThreadLocal("test_domain");

        graphBasedSequenceHandler.handle(req, resp, context);

        List<AuthHistory> authHistories = context.getAuthenticationStepHistory();
        assertNotNull(authHistories);
        assertEquals(authHistories.size(), authHistoryCount);
    }

    @DataProvider(name = "noJsDataProvider")
    public Object[][] getIdpMappedUserRolesData() {

        return new Object[][]{
                {"no-js-sp-1.xml", 3},
                {"disabled-js-sp-1.xml", 3},
        };
    }
}

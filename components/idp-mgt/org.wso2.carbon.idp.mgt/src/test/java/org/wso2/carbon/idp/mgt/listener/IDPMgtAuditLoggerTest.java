/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt.listener;

import org.junit.Assert;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.framework.common.testng.WithCarbonHome;

/**
 * Unit tests for IDPMgtAuditLogger.
 */
@WithCarbonHome
public class IDPMgtAuditLoggerTest extends PowerMockTestCase {

    IDPMgtAuditLogger idpMgtAuditLogger = new IDPMgtAuditLogger();

    @Test
    public void testGetDefaultOrderId() {

        Assert.assertEquals(220, idpMgtAuditLogger.getDefaultOrderId());
    }

    @Test
    public void testDoPostAddIdP() throws Exception {

        IdentityProvider provider = new IdentityProvider();
        provider.setIdentityProviderName("test");
        provider.setResourceId("1234");
        provider.setId("1");
        Assert.assertTrue(idpMgtAuditLogger.doPostAddIdP(provider, "carbon.super"));
    }

    @Test
    public void testDoPostUpdateIdP() throws Exception {

        IdentityProvider provider = new IdentityProvider();
        provider.setIdentityProviderName("test1");
        provider.setResourceId("1234");
        provider.setId("1");
        Assert.assertTrue(idpMgtAuditLogger.doPostUpdateIdP("test", provider, "carbon.super"));
    }

    @Test
    public void testDoPostDeleteIdPByResourceId() throws Exception {

        IdentityProvider provider = new IdentityProvider();
        provider.setIdentityProviderName("test1");
        provider.setResourceId("1234");
        provider.setId("1");
        Assert.assertTrue(idpMgtAuditLogger.doPostDeleteIdPByResourceId("1234", provider, "carbon.super"));
    }

    @Test
    public void doPostDeleteIdPs() throws Exception {

        Assert.assertTrue(idpMgtAuditLogger.doPostDeleteIdPs("carbon.super"));
    }
}

/*
 * Copyright (c) 2021-2026, WSO2 LLC. (https://www.wso2.com).
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

import org.apache.commons.logging.Log;
import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for IDPMgtAuditLogger.
 */
@WithCarbonHome
public class IDPMgtAuditLoggerTest {

    IDPMgtAuditLogger idpMgtAuditLogger = new IDPMgtAuditLogger();

    @Mock
    private Log mockAuditLog;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        // Inject mock into the package-private audit field.
        idpMgtAuditLogger.audit = mockAuditLog;
    }

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

    @Test
    public void testDoPostUpdateResidentIdPWithSessionProperties() throws Exception {

        IdentityProvider provider = new IdentityProvider();
        provider.setIdentityProviderName("LOCAL");
        provider.setResourceId("resident-idp-1");

        IdentityProviderProperty enableMaxSession = new IdentityProviderProperty();
        enableMaxSession.setName(IdentityApplicationConstants.ENABLE_MAXIMUM_SESSION_TIME_OUT);
        enableMaxSession.setValue("true");

        IdentityProviderProperty maxSessionTimeout = new IdentityProviderProperty();
        maxSessionTimeout.setName(IdentityApplicationConstants.MAXIMUM_SESSION_TIME_OUT);
        maxSessionTimeout.setValue("20160");

        provider.setIdpProperties(new IdentityProviderProperty[]{enableMaxSession, maxSessionTimeout});

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        Assert.assertTrue(idpMgtAuditLogger.doPostUpdateResidentIdP(provider, "carbon.super"));

        verify(mockAuditLog).info(logCaptor.capture());
        String logMessage = logCaptor.getValue();
        Assert.assertTrue("Audit log must contain EnableMaximumSessionTimeout",
                logMessage.contains(IdentityApplicationConstants.ENABLE_MAXIMUM_SESSION_TIME_OUT));
        Assert.assertTrue("Audit log must contain MaximumSessionTimeout",
                logMessage.contains(IdentityApplicationConstants.MAXIMUM_SESSION_TIME_OUT));
    }

    @Test
    public void testDoPostUpdateResidentIdPFiltersNonAllowedProperties() throws Exception {

        IdentityProvider provider = new IdentityProvider();
        provider.setIdentityProviderName("LOCAL");
        provider.setResourceId("resident-idp-1");

        // Include an allowed property alongside a non-allowed one.
        IdentityProviderProperty maxSessionTimeout = new IdentityProviderProperty();
        maxSessionTimeout.setName(IdentityApplicationConstants.MAXIMUM_SESSION_TIME_OUT);
        maxSessionTimeout.setValue("20160");

        IdentityProviderProperty otherProperty = new IdentityProviderProperty();
        otherProperty.setName("SomeOtherInternalProperty");
        otherProperty.setValue("sensitiveValue");

        provider.setIdpProperties(new IdentityProviderProperty[]{maxSessionTimeout, otherProperty});

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        Assert.assertTrue(idpMgtAuditLogger.doPostUpdateResidentIdP(provider, "carbon.super"));

        verify(mockAuditLog).info(logCaptor.capture());
        String logMessage = logCaptor.getValue();
        Assert.assertTrue("Audit log must contain the allowed MaximumSessionTimeout property",
                logMessage.contains(IdentityApplicationConstants.MAXIMUM_SESSION_TIME_OUT));
        Assert.assertFalse("Audit log must NOT contain the non-allowed SomeOtherInternalProperty",
                logMessage.contains("SomeOtherInternalProperty"));
        Assert.assertFalse("Audit log must NOT contain the sensitive value of a non-allowed property",
                logMessage.contains("sensitiveValue"));
    }

    @Test
    public void testDoPostUpdateResidentIdPWithNoIdpProperties() throws Exception {

        IdentityProvider provider = new IdentityProvider();
        provider.setIdentityProviderName("LOCAL");
        provider.setResourceId("resident-idp-1");
        Assert.assertTrue(idpMgtAuditLogger.doPostUpdateResidentIdP(provider, "carbon.super"));
    }

    @Test
    public void testDoPostUpdateResidentIdPWithNullResourceId() throws Exception {

        // When the resource ID is null/blank, it should fall back to "Undefined" without throwing.
        IdentityProvider provider = new IdentityProvider();
        provider.setIdentityProviderName("LOCAL");
        Assert.assertTrue(idpMgtAuditLogger.doPostUpdateResidentIdP(provider, "carbon.super"));
    }

    @Test
    public void testDoPostUpdateResidentIdPWithNullProvider() throws Exception {

        Assert.assertTrue(idpMgtAuditLogger.doPostUpdateResidentIdP(null, "carbon.super"));
    }
}

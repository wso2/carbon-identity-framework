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

package org.wso2.carbon.idp.mgt.util;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ErrorMessage;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.Assert.assertEquals;

import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.REMEMBER_ME_TIME_OUT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.REMEMBER_ME_TIME_OUT_DEFAULT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.SESSION_IDLE_TIME_OUT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.SESSION_IDLE_TIME_OUT_DEFAULT;

/**
 * Unit tests for IdPManagementUtil.
 */
@WithCarbonHome
@PrepareForTest({IdentityUtil.class, IdentityApplicationConstants.class, IdentityProviderManager.class,
        IdentityApplicationManagementUtil.class, IdPManagementServiceComponent.class})
@PowerMockIgnore({"javax.net.*", "javax.security.*", "javax.crypto.*", "javax.xml.*", "org.xml.sax.*", "org.w3c.dom" +
        ".*", "org.apache.xerces.*","org.mockito.*"})
public class IdPManagementUtilTest extends PowerMockTestCase {

    @Mock
    private IdentityProviderManager mockedIdentityProviderManager;
    @Mock
    private IdentityProvider mockedIdentityProvider;
    @Mock
    private IdentityProviderProperty mockedIdentityProviderProperty;
    @Mock
    private TenantManager mockedTenantManager;
    @Mock
    private RealmService mockedRealmService;

    @DataProvider
    public Object[][] getTenantIdOfDomainData() {

        return new Object[][]{
                {"tenantDomain", 2, "success"},
                {null, 1, "fail"},
        };
    }

    @Test(dataProvider = "getTenantIdOfDomainData")
    public void testGetTenantIdOfDomain(String tenantDomain, int tenantId, String expectedResult) throws Exception {

        mockStatic(IdentityUtil.class);
        mockStatic(IdPManagementServiceComponent.class);
        when(IdPManagementServiceComponent.getRealmService()).thenReturn(mockedRealmService);
        when(mockedRealmService.getTenantManager()).thenReturn(mockedTenantManager);
        when(mockedTenantManager.getTenantId(tenantDomain)).thenReturn(tenantId);

        String result;
        try {
            int id = IdPManagementUtil.getTenantIdOfDomain(tenantDomain);
            assertEquals(id, tenantId);
            result = "success";
        } catch (IllegalArgumentException e) {
            result = "fail";
        }
        assertEquals(result, expectedResult);
    }

    @DataProvider
    public Object[][] getResidentIdPEntityIdData() {

        final String customRegEx = "^[a-zA-Z0-9]+";
        return new Object[][]{
                {null, "localhost"},
                {"", "localhost"},
                {"12fqwe34", "12fqwe34"},
                {customRegEx, customRegEx},
        };
    }

    @Test(dataProvider = "getResidentIdPEntityIdData")
    public void testGetResidentIdPEntityId(String localEntityId, String expectedEntityId) {

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getProperty("SSOService.EntityId")).thenReturn(localEntityId);
        assertEquals(IdPManagementUtil.getResidentIdPEntityId(), expectedEntityId);
    }

    @DataProvider
    public Object[][] getIdleSessionTimeOutData() {

        final int timeout = Integer.parseInt(SESSION_IDLE_TIME_OUT_DEFAULT) * 60;
        return new Object[][]{
                {"carbon.super", true, "10", 10 * 60},
                {"test1", true, "15", 15 * 60},
                {"testnull", false, "5", timeout},
        };
    }

    @Test(dataProvider = "getIdleSessionTimeOutData")
    public void testGetIdleSessionTimeOut(String tenetDomain, boolean validity, String value, int timeOut)
            throws Exception {

        mockStatic(IdentityProviderManager.class);
        mockStatic(IdentityUtil.class);
        mockStatic(IdentityApplicationManagementUtil.class);

        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[0];

        when(IdentityProviderManager.getInstance()).thenReturn(mockedIdentityProviderManager);
        when(mockedIdentityProviderManager.getResidentIdP(tenetDomain)).thenReturn(mockedIdentityProvider);
        when(mockedIdentityProvider.getIdpProperties()).thenReturn(idpProperties);

        if (validity) {
            when(IdentityApplicationManagementUtil.getProperty(mockedIdentityProvider.getIdpProperties(),
                    SESSION_IDLE_TIME_OUT)).thenReturn(mockedIdentityProviderProperty);
        } else {
            when(IdentityApplicationManagementUtil.getProperty(mockedIdentityProvider.getIdpProperties(),
                    SESSION_IDLE_TIME_OUT)).thenReturn(null);
        }

        when(mockedIdentityProviderProperty.getValue()).thenReturn(value);
        assertEquals(IdPManagementUtil.getIdleSessionTimeOut(tenetDomain), timeOut);
    }

    @DataProvider
    public Object[][] getRememberMeTimeoutData() {

        final int timeout = Integer.parseInt(REMEMBER_ME_TIME_OUT_DEFAULT) * 60;
        return new Object[][]{
                {"carbon.super", true, "10", 10 * 60},
                {"test1", true, "15", 15 * 60},
                {"testnull", false, "5", timeout},
        };
    }

    @Test(dataProvider = "getRememberMeTimeoutData")
    public void testGetRememberMeTimeout(String tenetDomain, boolean validity, String value, int timeOut)
            throws Exception {

        mockStatic(IdentityProviderManager.class);
        mockStatic(IdentityUtil.class);
        mockStatic(IdentityApplicationManagementUtil.class);

        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[0];

        when(IdentityProviderManager.getInstance()).thenReturn(mockedIdentityProviderManager);
        when(mockedIdentityProviderManager.getResidentIdP(anyString())).thenReturn(mockedIdentityProvider);
        when(mockedIdentityProvider.getIdpProperties()).thenReturn(idpProperties);

        if (validity) {
            when(IdentityApplicationManagementUtil.getProperty(mockedIdentityProvider.getIdpProperties(),
                    REMEMBER_ME_TIME_OUT)).thenReturn(mockedIdentityProviderProperty);
        } else {
            when(IdentityApplicationManagementUtil.getProperty(mockedIdentityProvider.getIdpProperties(),
                    REMEMBER_ME_TIME_OUT)).thenReturn(null);
        }

        when(mockedIdentityProviderProperty.getValue()).thenReturn(value);
        assertEquals(IdPManagementUtil.getRememberMeTimeout(tenetDomain), timeOut);
    }

    @DataProvider
    public Object[][] setTenantSpecifiersData() {

        return new Object[][]{
                {"carbon.super", "", ""},
                {"test", "t/test/", "?tenantDomain=test"},
        };
    }

    @Test(dataProvider = "setTenantSpecifiersData")
    public void testSetTenantSpecifiers(String tenantDomain, String tenantContext, String tenantParameter) {

        IdPManagementUtil.setTenantSpecifiers(tenantDomain);
        assertEquals(IdPManagementUtil.getTenantContext(), tenantContext);
        assertEquals(IdPManagementUtil.getTenantParameter(), tenantParameter);
    }

    @Test
    public void testHandleClientException() {

        IdentityProviderManagementClientException exception1 =
                IdPManagementUtil.handleClientException(ErrorMessage.ERROR_CODE_ADD_IDP, "");
        assertEquals(exception1.getErrorCode(), "IDP-65002");
        assertEquals(exception1.getMessage(), "Error while adding the Identity Provider: %s.");

        Throwable t = new Throwable();
        IdentityProviderManagementClientException exception2 =
                IdPManagementUtil.handleClientException(ErrorMessage.ERROR_CODE_ADD_IDP, "test2", t);
        assertEquals(exception2.getErrorCode(), "IDP-65002");
        assertEquals(exception2.getMessage(), "Error while adding the Identity Provider: test2.");
    }

    @Test
    public void testHandleServerException() {

        IdentityProviderManagementServerException exception1 =
                IdPManagementUtil.handleServerException(ErrorMessage.ERROR_CODE_ADD_IDP, "test1");
        assertEquals(exception1.getErrorCode(), "IDP-65002");
        assertEquals(exception1.getMessage(), "Error while adding the Identity Provider: test1.");

        Throwable t = new Throwable();
        IdentityProviderManagementServerException exception2 =
                IdPManagementUtil.handleServerException(ErrorMessage.ERROR_CODE_ADD_IDP, "test2", t);
        assertEquals(exception2.getErrorCode(), "IDP-65002");
        assertEquals(exception2.getMessage(), "Error while adding the Identity Provider: test2.");
    }
}

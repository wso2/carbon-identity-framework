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
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.fail;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.REMEMBER_ME_TIME_OUT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.REMEMBER_ME_TIME_OUT_DEFAULT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.SESSION_IDLE_TIME_OUT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.SESSION_IDLE_TIME_OUT_DEFAULT;

/**
 * Unit tests for IdPManagementUtil.
 */
@WithCarbonHome
@Listeners(MockitoTestNGListener.class)
public class IdPManagementUtilTest {

    private static final String TRUE_STRING = "true";
    private static final String FALSE_STRING = "false";

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

        try (MockedStatic<IdPManagementServiceComponent> idPManagementServiceComponent =
                     mockStatic(IdPManagementServiceComponent.class)) {
            idPManagementServiceComponent.when(
                    IdPManagementServiceComponent::getRealmService).thenReturn(mockedRealmService);
            lenient().when(mockedRealmService.getTenantManager()).thenReturn(mockedTenantManager);
            lenient().when(mockedTenantManager.getTenantId(tenantDomain)).thenReturn(tenantId);

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

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty("SSOService.EntityId")).thenReturn(localEntityId);
            assertEquals(IdPManagementUtil.getResidentIdPEntityId(), expectedEntityId);
        }
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

        try (MockedStatic<IdentityProviderManager> identityProviderManager =
                     mockStatic(IdentityProviderManager.class);
             MockedStatic<IdentityApplicationManagementUtil> identityApplicationManagementUtil =
                     mockStatic(IdentityApplicationManagementUtil.class)) {

            IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[0];

            identityProviderManager.when(IdentityProviderManager::getInstance)
                    .thenReturn(mockedIdentityProviderManager);
            when(mockedIdentityProviderManager.getResidentIdP(tenetDomain)).thenReturn(mockedIdentityProvider);
            when(mockedIdentityProvider.getIdpProperties()).thenReturn(idpProperties);

            if (validity) {
                identityApplicationManagementUtil.when(
                        () -> IdentityApplicationManagementUtil.getProperty(mockedIdentityProvider.getIdpProperties(),
                                SESSION_IDLE_TIME_OUT)).thenReturn(mockedIdentityProviderProperty);
            } else {
                identityApplicationManagementUtil.when(
                        () -> IdentityApplicationManagementUtil.getProperty(mockedIdentityProvider.getIdpProperties(),
                                SESSION_IDLE_TIME_OUT)).thenReturn(null);
            }

            lenient().when(mockedIdentityProviderProperty.getValue()).thenReturn(value);
            assertEquals(IdPManagementUtil.getIdleSessionTimeOut(tenetDomain), timeOut);
        }
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

        try (MockedStatic<IdentityProviderManager> identityProviderManager =
                     mockStatic(IdentityProviderManager.class);
             MockedStatic<IdentityApplicationManagementUtil> identityApplicationManagementUtil =
                     mockStatic(IdentityApplicationManagementUtil.class)) {

            IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[0];

            identityProviderManager.when(IdentityProviderManager::getInstance)
                    .thenReturn(mockedIdentityProviderManager);
            when(mockedIdentityProviderManager.getResidentIdP(anyString())).thenReturn(mockedIdentityProvider);
            when(mockedIdentityProvider.getIdpProperties()).thenReturn(idpProperties);

            if (validity) {
                identityApplicationManagementUtil.when(
                        () -> IdentityApplicationManagementUtil.getProperty(mockedIdentityProvider.getIdpProperties(),
                                REMEMBER_ME_TIME_OUT)).thenReturn(mockedIdentityProviderProperty);
            } else {
                identityApplicationManagementUtil.when(
                        () -> IdentityApplicationManagementUtil.getProperty(mockedIdentityProvider.getIdpProperties(),
                                REMEMBER_ME_TIME_OUT)).thenReturn(null);
            }

            lenient().when(mockedIdentityProviderProperty.getValue()).thenReturn(value);
            assertEquals(IdPManagementUtil.getRememberMeTimeout(tenetDomain), timeOut);
        }
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

    @Test
    public void testExceptionValidateUsernameRecoveryPropertyValues() {

        Map<String, String> configDetails1 = new HashMap<>();
        configDetails1.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, TRUE_STRING);
        configDetails1.put(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY, FALSE_STRING);
        configDetails1.put(IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY, FALSE_STRING);

        try {
            IdPManagementUtil.validateUsernameRecoveryPropertyValues(configDetails1);
            fail("Expected an IdentityProviderManagementServerException to be thrown");
        } catch (IdentityProviderManagementClientException e) {
            assertEquals(e.getErrorCode(),
                    IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION.getCode());
        }

        Map<String, String> configDetails2 = new HashMap<>();
        configDetails2.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, FALSE_STRING);
        configDetails2.put(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY, TRUE_STRING);
        try {
            IdPManagementUtil.validateUsernameRecoveryPropertyValues(configDetails2);
            fail("Expected an IdentityProviderManagementServerException to be thrown");
        } catch (IdentityProviderManagementClientException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION.getCode());
        }

        Map<String, String> configDetails3 = new HashMap<>();
        configDetails3.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, FALSE_STRING);
        configDetails3.put(IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY, TRUE_STRING);
        try {
            IdPManagementUtil.validateUsernameRecoveryPropertyValues(configDetails3);
            fail("Expected an IdentityProviderManagementServerException to be thrown");
        } catch (IdentityProviderManagementClientException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION.getCode());
        }

    }

    @Test(dataProvider = "passwordRecoveryConfigs")
    public void testValidatePasswordRecoveryPropertyValues(HashMap<String, String> configs, boolean isValid) {

        try {
            IdPManagementUtil.validatePasswordRecoveryPropertyValues(configs);

            if (!isValid) {
                Assert.fail("Expected an  IdentityProviderManagementClientException but no exception was thrown.");
            }
        } catch (IdentityProviderManagementClientException e) {
            if (isValid) {
                Assert.fail("Did not expect IdentityProviderManagementClientException but one was thrown.", e);
            }
        }

    }

    @DataProvider(name = "passwordRecoveryConfigs")
    public Object[][] setPasswordRecoveryConfigs() {

        return new Object[][]{
                {getPasswordRecoveryConfigs(true, true, null, null), true},
                {getPasswordRecoveryConfigs(null, true, null, null), true},
                {getPasswordRecoveryConfigs(null, null, true, null), true},
                {getPasswordRecoveryConfigs(null, null, null, true), true},
                {getPasswordRecoveryConfigs(true, null, null, null), true},
                {getPasswordRecoveryConfigs(true, false, true, null), true},
                {getPasswordRecoveryConfigs(true, false, null, null), true},
                {getPasswordRecoveryConfigs(true, false, false, null), true},
                {getPasswordRecoveryConfigs(true, false, false, true), true},
                {getPasswordRecoveryConfigs(true, false, false, false), false},
                {getPasswordRecoveryConfigs(false, true, null, null), false},
                {getPasswordRecoveryConfigs(false, false, true, false), false},
                {getPasswordRecoveryConfigs(false, false, false, true), false}

        };
    }

    private HashMap<String, String> getPasswordRecoveryConfigs(Boolean passwordRecoveryEnable,
                                                               Boolean passwordRecoveryEmailLinkEnable,
                                                               Boolean passwordRecoveryEmailOtpEnable,
                                                               Boolean passwordRecoverySmsOtpEnable) {

        HashMap<String, String> configs = new HashMap<>();

        if (passwordRecoveryEnable != null) {
            configs.put(IdPManagementConstants.NOTIFICATION_PASSWORD_ENABLE_PROPERTY, passwordRecoveryEnable ?
                    TRUE_STRING : FALSE_STRING);
        }

        if (passwordRecoveryEmailLinkEnable != null) {
            configs.put(IdPManagementConstants.EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY,
                    passwordRecoveryEmailLinkEnable ? TRUE_STRING : FALSE_STRING);
        }

        if (passwordRecoveryEmailOtpEnable != null) {
            configs.put(IdPManagementConstants.EMAIL_OTP_PASSWORD_RECOVERY_PROPERTY, passwordRecoveryEmailOtpEnable ?
                    TRUE_STRING : FALSE_STRING);
        }

        if (passwordRecoverySmsOtpEnable != null) {
            configs.put(IdPManagementConstants.SMS_OTP_PASSWORD_RECOVERY_PROPERTY, passwordRecoverySmsOtpEnable ?
                    TRUE_STRING : FALSE_STRING);
        }

        return configs;
    }

    @Test(dataProvider = "setSuccessConfigDetails")
    public void testSuccessVerificationsInValidateUsernameRecoveryPropertyValues(Map<String, String> configDetails)
            throws IdentityProviderManagementClientException {

        IdPManagementUtil.validateUsernameRecoveryPropertyValues(configDetails);
    }

    @DataProvider(name = "setSuccessConfigDetails")
    public Object[][] setSuccessConfigDetails() {

        Map<String, String> configDetails1 = new HashMap<>();
        configDetails1.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, TRUE_STRING);
        configDetails1.put(IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY, TRUE_STRING);
        configDetails1.put(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY, TRUE_STRING);

        Map<String, String> configDetails2 = new HashMap<>();
        configDetails2.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, TRUE_STRING);
        configDetails2.put(IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY, TRUE_STRING);
        configDetails2.put(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY, FALSE_STRING);

        Map<String, String> configDetails3 = new HashMap<>();
        configDetails3.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, TRUE_STRING);
        configDetails3.put(IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY, FALSE_STRING);
        configDetails3.put(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY, TRUE_STRING);

        Map<String, String> configDetails4 = new HashMap<>();
        configDetails4.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, TRUE_STRING);
        configDetails4.put(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY, TRUE_STRING);

        Map<String, String> configDetails5 = new HashMap<>();
        configDetails5.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, TRUE_STRING);
        configDetails5.put(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY, FALSE_STRING);

        Map<String, String> configDetails6 = new HashMap<>();
        configDetails6.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, TRUE_STRING);

        Map<String, String> configDetails7 = new HashMap<>();
        configDetails7.put(IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY, TRUE_STRING);

        Map<String, String> configDetails8 = new HashMap<>();
        configDetails8.put(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY, TRUE_STRING);

        Map<String, String> configDetails9 = new HashMap<>();
        configDetails9.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, FALSE_STRING);

        Map<String, String> configDetails10 = new HashMap<>();
        configDetails10.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, FALSE_STRING);
        configDetails10.put(IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY, FALSE_STRING);

        Map<String, String> configDetails11 = new HashMap<>();
        configDetails11.put(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY, FALSE_STRING);
        configDetails11.put(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY, FALSE_STRING);

        return new Object[][]{
                {configDetails1},
                {configDetails2},
                {configDetails3},
                {configDetails4},
                {configDetails5},
                {configDetails6},
                {configDetails7},
                {configDetails8},
                {configDetails9},
                {configDetails10},
                {configDetails11}
        };
    }

}

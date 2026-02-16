/*
 * Copyright (c) 2021-2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ErrorMessage;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.fail;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.PRESERVE_CURRENT_SESSION_AT_PASSWORD_UPDATE;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.REMEMBER_ME_TIME_OUT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.REMEMBER_ME_TIME_OUT_DEFAULT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.SESSION_IDLE_TIME_OUT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.SESSION_IDLE_TIME_OUT_DEFAULT;
import static org.wso2.carbon.identity.base.IdentityConstants.ServerConfig.PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_OFFLINE_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_EMAIL_LINK_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_EMAIL_OTP_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ASK_PASSWORD_SEND_EMAIL_OTP;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ASK_PASSWORD_SEND_SMS_OTP;

/**
 * Unit tests for IdPManagementUtil.
 */
@WithCarbonHome
@Listeners(MockitoTestNGListener.class)
public class IdPManagementUtilTest {

    private static final String TRUE_STRING = "true";
    private static final String FALSE_STRING = "false";
    private static final String PASSWORD_RECOVERY_ENABLE = "Recovery.Notification.Password.Enable";
    private static final String PASSWORD_RECOVERY_EMAIL_LINK_ENABLE
            = "Recovery.Notification.Password.emailLink.Enable";
    private static final String PASSWORD_RECOVERY_EMAIL_OTP_ENABLE =
            "Recovery.Notification.Password.OTP.SendOTPInEmail";
    private static final String PASSWORD_RECOVERY_SMS_OTP_ENABLE = "Recovery.Notification.Password.smsOtp.Enable";
    private static final String IDP_NAME = "testIdP";
    private static final int TENANT_ID = 1;
    private static final String TENANT_DOMAIN = "test.com";

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
    @Mock
    private CacheBackedIdPMgtDAO mockDao;

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
    public void testClearIdPCache() throws Exception {

        try (MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class)) {
            identityTenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN))
                    .thenReturn(TENANT_ID);

            // Since the dao is a private final field, using reflection to change the access modifier and
            // replace it with a mock object. Since this is a simple test, this won't introduce any complexities.
            Field daoField = IdPManagementUtil.class.getDeclaredField("CACHE_BACKED_IDP_MGT_DAO");
            daoField.setAccessible(true);

            // Store the original value to restore later
            Object originalDao = daoField.get(null);

            // Use Unsafe to modify static final fields in Java 12+
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

            Object fieldBase = unsafe.staticFieldBase(daoField);
            long fieldOffset = unsafe.staticFieldOffset(daoField);
            unsafe.putObject(fieldBase, fieldOffset, mockDao);

            // Verify the field was set correctly
            Object injectedDao = daoField.get(null);
            assertEquals(injectedDao, mockDao, "Mock DAO should be injected");

            try {
                IdPManagementUtil.clearIdPCache(IDP_NAME, TENANT_DOMAIN);
                verify(mockDao, times(1)).clearIdpCache(IDP_NAME, TENANT_ID, TENANT_DOMAIN);

                doThrow(new IdentityProviderManagementException("Test exception")).when(mockDao)
                        .clearIdpCache(anyString(), anyInt(), anyString());
                // Checking if the exception is handled gracefully.
                IdPManagementUtil.clearIdPCache(IDP_NAME, TENANT_DOMAIN);
                verify(mockDao, times(2)).clearIdpCache(IDP_NAME, TENANT_ID, TENANT_DOMAIN);
            } finally {
                // Restore the original DAO
                unsafe.putObject(fieldBase, fieldOffset, originalDao);
            }
        }
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
                Assert.fail("Did not expect IdentityProviderManagementClientException.", e);
            }
        }
    }

    @Test(dataProvider = "adminPasswordResetConfigs")
    public void testValidateAdminPasswordResetWithCurrentAndPreviousConfigs(HashMap<String, String> configs, IdentityProviderProperty[] identityProviderProperties,boolean isValid) {

        try {
            IdPManagementUtil.validateAdminPasswordResetWithCurrentAndPreviousConfigs(configs,
                    identityProviderProperties);

            if (!isValid) {
                Assert.fail("Expected an  IdentityProviderManagementClientException but no exception was thrown.");
            }
        } catch (IdentityProviderManagementClientException e) {
            if (isValid) {
                Assert.fail("Did not expect IdentityProviderManagementClientException.", e);
            }
        }
    }

    @Test(dataProvider = "askPasswordConfigs")
    public void testValidateAskPasswordBasedPasswordSetWithCurrentAndPreviousConfigs(HashMap<String, String> configs, IdentityProviderProperty[] identityProviderProperties, boolean isValid) {

        try {
            IdPManagementUtil.validateAskPasswordBasedPasswordSetWithCurrentAndPreviousConfigs(configs,
                    identityProviderProperties);

            if (!isValid) {
                Assert.fail("Expected an  IdentityProviderManagementClientException but no exception was thrown.");
            }
        } catch (IdentityProviderManagementClientException e) {
            if (isValid) {
                Assert.fail("Did not expect IdentityProviderManagementClientException.", e);
            }
        }
    }

    @Test
    public void testValidateAskPasswordBasedPasswordSetWithNonAskPasswordConfigs() {

        // Test with configurations that don't contain ask password keys
        HashMap<String, String> nonAskPasswordConfigs = new HashMap<>();
        nonAskPasswordConfigs.put("SomeOtherProperty", "true");
        nonAskPasswordConfigs.put("AnotherProperty", "false");

        IdentityProviderProperty[] emptyProps = new IdentityProviderProperty[0];

        try {
            // This should not throw any exception as no ask password keys are present
            IdPManagementUtil.validateAskPasswordBasedPasswordSetWithCurrentAndPreviousConfigs(
                    nonAskPasswordConfigs, emptyProps);
        } catch (IdentityProviderManagementClientException e) {
            Assert.fail("Did not expect IdentityProviderManagementClientException for non-ask password configs.", e);
        }
    }

    @Test(dataProvider = "passwordRecoveryConfigsWithIdpMgtProps")
    public void testValidatePasswordRecoveryWithCurrentAndPreviousConfigs(HashMap<String, String> configs,
                                                                          IdentityProviderProperty[] identityProviderProperties,
                                                                          boolean isValid) {

        try {
            IdPManagementUtil.validatePasswordRecoveryWithCurrentAndPreviousConfigs(configs,
                    identityProviderProperties);

            if (!isValid) {
                Assert.fail("Expected an  IdentityProviderManagementClientException but no exception was thrown.");
            }
        } catch (IdentityProviderManagementClientException e) {
            if (isValid) {
                Assert.fail("Did not expect IdentityProviderManagementClientException.", e);
            }
        }

    }

    @DataProvider(name = "adminPasswordResetConfigs")
    public Object[][] setAdminPasswordResetConfigs() {

        IdentityProviderProperty[] adminPasswordResetIdentityPropsAllFalse =
                getAdminPasswordResetIdentityProviderProperties(false, false, false, false);

        IdentityProviderProperty[] adminPasswordResetIdentityPropsEmailLinkEnabled =
                getAdminPasswordResetIdentityProviderProperties(true, false, false, false);

        IdentityProviderProperty[] adminPasswordResetIdentityPropsEmailOtpEnabled =
                getAdminPasswordResetIdentityProviderProperties(false, true, false, false);

        IdentityProviderProperty[] adminPasswordResetIdentityPropsOfflineEnabled =
                getAdminPasswordResetIdentityProviderProperties(false, false, true, false);

        HashMap<String, String> adminPasswordResetConfig1 = getAdminPasswordResetConfigs(true, null, null, null);

        HashMap<String, String> adminPasswordResetConfig2 = getAdminPasswordResetConfigs(null, true, null, null);

        HashMap<String, String> adminPasswordResetConfig3 = getAdminPasswordResetConfigs(null, null, true, null);

        HashMap<String, String> adminPasswordResetConfig4 = getAdminPasswordResetConfigs(null, null, null, null);

        HashMap<String, String> adminPasswordResetConfig5 = getAdminPasswordResetConfigs(true, true, null, null);

        HashMap<String, String> adminPasswordResetConfig6 = getAdminPasswordResetConfigs(null, true, null, null);

        HashMap<String, String> adminPasswordResetConfig7 = getAdminPasswordResetConfigs(null, null, true, null);

        HashMap<String, String> adminPasswordResetConfig8 = getAdminPasswordResetConfigs(false, true, null, null);

        HashMap<String, String> adminPasswordResetConfig9 = getAdminPasswordResetConfigs(false, null, null, null);

        HashMap<String, String> adminPasswordResetConfig10 = getAdminPasswordResetConfigs(null, null, true, null);

        HashMap<String, String> adminPasswordResetConfig11 = getAdminPasswordResetConfigs(true, null, null, null);

        HashMap<String, String> adminPasswordResetConfig12 = getAdminPasswordResetConfigs(null, null, null, true);

        return new Object[][]{
                // configs, identityProviderProperties, isValid
                {adminPasswordResetConfig1, adminPasswordResetIdentityPropsAllFalse, true},
                {adminPasswordResetConfig2, adminPasswordResetIdentityPropsAllFalse, true},
                {adminPasswordResetConfig3, adminPasswordResetIdentityPropsAllFalse, true},
                {adminPasswordResetConfig4, adminPasswordResetIdentityPropsAllFalse, true},
                {adminPasswordResetConfig5, adminPasswordResetIdentityPropsAllFalse, false},
                {adminPasswordResetConfig6, adminPasswordResetIdentityPropsEmailLinkEnabled, false},
                {adminPasswordResetConfig7, adminPasswordResetIdentityPropsEmailLinkEnabled, false},
                {adminPasswordResetConfig8, adminPasswordResetIdentityPropsEmailLinkEnabled, true},
                {adminPasswordResetConfig9, adminPasswordResetIdentityPropsEmailLinkEnabled, false},
                {adminPasswordResetConfig10, adminPasswordResetIdentityPropsEmailOtpEnabled, false},
                {adminPasswordResetConfig11, adminPasswordResetIdentityPropsOfflineEnabled, false},
                {adminPasswordResetConfig12, adminPasswordResetIdentityPropsAllFalse, true},
                {adminPasswordResetConfig12, adminPasswordResetIdentityPropsEmailOtpEnabled, false}
        };
    }

    @DataProvider(name = "askPasswordConfigs")
    public Object[][] setAskPasswordConfigs() {

        IdentityProviderProperty[] askPasswordIdentityPropsAllFalse =
                getAskPasswordIdentityProviderProperties(false, false);

        IdentityProviderProperty[] askPasswordIdentityPropsEmailOtpEnabled =
                getAskPasswordIdentityProviderProperties(true, false);

        IdentityProviderProperty[] askPasswordIdentityPropsSmsOtpEnabled =
                getAskPasswordIdentityProviderProperties(false, true);

        HashMap<String, String> askPasswordConfig1 = getAskPasswordConfigs(true, null);

        HashMap<String, String> askPasswordConfig2 = getAskPasswordConfigs(null, true);

        HashMap<String, String> askPasswordConfig3 = getAskPasswordConfigs(null, null);

        HashMap<String, String> askPasswordConfig4 = getAskPasswordConfigs(true, true);

        HashMap<String, String> askPasswordConfig5 = getAskPasswordConfigs(false, true);

        HashMap<String, String> askPasswordConfig6 = getAskPasswordConfigs(true, false);

        HashMap<String, String> askPasswordConfig7 = getAskPasswordConfigs(false, null);

        HashMap<String, String> askPasswordConfig8 = getAskPasswordConfigs(null, false);

        HashMap<String, String> askPasswordConfig9 = getAskPasswordConfigs(false, false);

        return new Object[][]{
                // configs, identityProviderProperties, isValid
                {askPasswordConfig1, askPasswordIdentityPropsAllFalse, true},
                {askPasswordConfig2, askPasswordIdentityPropsAllFalse, true},
                {askPasswordConfig3, askPasswordIdentityPropsAllFalse, true},
                {askPasswordConfig4, askPasswordIdentityPropsAllFalse, false},
                {askPasswordConfig5, askPasswordIdentityPropsEmailOtpEnabled, true},
                {askPasswordConfig6, askPasswordIdentityPropsSmsOtpEnabled, true},
                {askPasswordConfig7, askPasswordIdentityPropsAllFalse, true},
                {askPasswordConfig8, askPasswordIdentityPropsAllFalse, true},
                {askPasswordConfig9, askPasswordIdentityPropsAllFalse, true},
                {askPasswordConfig1, askPasswordIdentityPropsSmsOtpEnabled, false},
                {askPasswordConfig2, askPasswordIdentityPropsEmailOtpEnabled, false}
        };
    }

    @DataProvider(name = "passwordRecoveryConfigsWithIdpMgtProps")
    public Object[][] setPasswordRecoveryConfigsWithIpdMgtProps() {

        IdentityProviderProperty[] passwordIdentityPropsAllFalse = getPasswordRecoveryIdentityProviderProperties(false,
                false, false, false);

        IdentityProviderProperty[] passwordIdentityPropsEmailLinkEnabled =
                getPasswordRecoveryIdentityProviderProperties(false, true, false, false);

        IdentityProviderProperty[] passwordIdentityPropsEmailOtpEnabled =
                getPasswordRecoveryIdentityProviderProperties(false, false, true, false);

        HashMap<String, String> recoveryConfig1 = getPasswordRecoveryConfigs(null, true, null, null);

        HashMap<String, String> recoveryConfig2 = getPasswordRecoveryConfigs(null, null, true, null);

        HashMap<String, String> recoveryConfig3 = getPasswordRecoveryConfigs(true, null, null, null);

        HashMap<String, String> recoveryConfig4 = getPasswordRecoveryConfigs(null, true, false, null);

        HashMap<String, String> recoveryConfig5 = getPasswordRecoveryConfigs(null, false, true, null);
        return new Object[][]{
                {recoveryConfig1, passwordIdentityPropsEmailOtpEnabled, false},
                {recoveryConfig1, passwordIdentityPropsAllFalse, true},
                {recoveryConfig2, passwordIdentityPropsEmailLinkEnabled, false},
                {recoveryConfig2, passwordIdentityPropsAllFalse, true},
                {recoveryConfig3, passwordIdentityPropsAllFalse, true},
                {recoveryConfig4, passwordIdentityPropsEmailOtpEnabled, true},
                {recoveryConfig5, passwordIdentityPropsEmailLinkEnabled, true}
        };
    }

    @DataProvider(name = "passwordRecoveryConfigs")
    public Object[][] setPasswordRecoveryConfigs() {

        HashMap<String, String> recoveryConfig1 = getPasswordRecoveryConfigs(true, true, null, null);

        HashMap<String, String> recoveryConfig2 = getPasswordRecoveryConfigs(true, false, false, false);

        HashMap<String, String> recoveryConfig3 = getPasswordRecoveryConfigs(false, true, false, false);

        HashMap<String, String> recoveryConfig4 = getPasswordRecoveryConfigs(false, false, true, false);

        HashMap<String, String> recoveryConfig5 = getPasswordRecoveryConfigs(false, false, false, true);

        HashMap<String, String> recoveryConfig6 = getPasswordRecoveryConfigs(null, true, null, null);

        HashMap<String, String> recoveryConfig7 = getPasswordRecoveryConfigs(null, null, null, true);

        HashMap<String, String> recoveryConfig8 = getPasswordRecoveryConfigs(null, false, null, null);

        HashMap<String, String> recoveryConfig9 = getPasswordRecoveryConfigs(null, true, false, null);

        HashMap<String, String> recoveryConfig10 = getPasswordRecoveryConfigs(null, false, true, null);

        HashMap<String, String> recoveryConfig11 = getPasswordRecoveryConfigs(null, true, true, false);

        HashMap<String, String> recoveryConfig12 = getPasswordRecoveryConfigs(null, null, true, null);

        return new Object[][]{
                {recoveryConfig1, true},
                {recoveryConfig2, false},
                {recoveryConfig3, false},
                {recoveryConfig4, false},
                {recoveryConfig5, false},
                {recoveryConfig6, true},
                {recoveryConfig7, true},
                {recoveryConfig8, true},
                {recoveryConfig9, true},
                {recoveryConfig10, true},
                {recoveryConfig11, false},
                {recoveryConfig12, true}
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

    private IdentityProviderProperty[] getPasswordRecoveryIdentityProviderProperties(
            boolean passwordRecoveryEnable,
            boolean passwordRecoveryEmailLinkEnable,
            boolean passwordRecoveryEmailOtpEnable,
            boolean passwordRecoverySmsOtpEnable) {

        IdentityProviderProperty identityProviderProperty1 = new IdentityProviderProperty();
        identityProviderProperty1.setName(PASSWORD_RECOVERY_ENABLE);
        identityProviderProperty1.setValue(passwordRecoveryEnable ? TRUE_STRING : FALSE_STRING);

        IdentityProviderProperty identityProviderProperty2 = new IdentityProviderProperty();
        identityProviderProperty2.setName(PASSWORD_RECOVERY_EMAIL_LINK_ENABLE);
        identityProviderProperty2.setValue(passwordRecoveryEmailLinkEnable ? TRUE_STRING : FALSE_STRING);

        IdentityProviderProperty identityProviderProperty3 = new IdentityProviderProperty();
        identityProviderProperty3.setName(PASSWORD_RECOVERY_EMAIL_OTP_ENABLE);
        identityProviderProperty3.setValue(passwordRecoveryEmailOtpEnable ? TRUE_STRING : FALSE_STRING);

        IdentityProviderProperty identityProviderProperty4 = new IdentityProviderProperty();
        identityProviderProperty4.setName(PASSWORD_RECOVERY_SMS_OTP_ENABLE);
        identityProviderProperty4.setValue(passwordRecoverySmsOtpEnable ? TRUE_STRING : FALSE_STRING);

        return new IdentityProviderProperty[]{
                identityProviderProperty1,
                identityProviderProperty2,
                identityProviderProperty3,
                identityProviderProperty4
        };
    }

    private HashMap<String, String> getAdminPasswordResetConfigs(Boolean isEmailLinkEnabled,
                                                                 Boolean isEmailOtpEnabled, Boolean isOfflineEnabled,
                                                                 Boolean isSmsOtpEnabled) {

        HashMap<String, String> configs = new HashMap<>();
        if (isEmailLinkEnabled != null) {
            configs.put(ENABLE_ADMIN_PASSWORD_RESET_EMAIL_LINK_PROPERTY,
                    isEmailLinkEnabled ? TRUE_STRING : FALSE_STRING);
        }
        if (isEmailOtpEnabled != null) {
            configs.put(ENABLE_ADMIN_PASSWORD_RESET_EMAIL_OTP_PROPERTY,
                    isEmailOtpEnabled ? TRUE_STRING : FALSE_STRING);
        }
        if (isOfflineEnabled != null) {
            configs.put(ENABLE_ADMIN_PASSWORD_RESET_OFFLINE_PROPERTY,
                    isOfflineEnabled ? TRUE_STRING : FALSE_STRING);
        }
        if (isSmsOtpEnabled != null) {
            configs.put(IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_SMS_OTP_PROPERTY,
                    isSmsOtpEnabled ? TRUE_STRING : FALSE_STRING);
        }
        return configs;
    }

    private IdentityProviderProperty[] getAdminPasswordResetIdentityProviderProperties(
            boolean isEmailLinkEnabled, boolean isEmailOtpEnabled, boolean isOfflineEnabled, boolean isSmsOtpEnabled) {

        IdentityProviderProperty identityProviderProperty1 = new IdentityProviderProperty();
        identityProviderProperty1.setName(ENABLE_ADMIN_PASSWORD_RESET_EMAIL_LINK_PROPERTY);
        identityProviderProperty1.setValue(isEmailLinkEnabled ? TRUE_STRING : FALSE_STRING);

        IdentityProviderProperty identityProviderProperty2 = new IdentityProviderProperty();
        identityProviderProperty2.setName(ENABLE_ADMIN_PASSWORD_RESET_EMAIL_OTP_PROPERTY);
        identityProviderProperty2.setValue(isEmailOtpEnabled ? TRUE_STRING : FALSE_STRING);

        IdentityProviderProperty identityProviderProperty3 = new IdentityProviderProperty();
        identityProviderProperty3.setName(ENABLE_ADMIN_PASSWORD_RESET_OFFLINE_PROPERTY);
        identityProviderProperty3.setValue(isOfflineEnabled ? TRUE_STRING : FALSE_STRING);

        IdentityProviderProperty identityProviderProperty4 = new IdentityProviderProperty();
        identityProviderProperty4.setName(IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_SMS_OTP_PROPERTY);
        identityProviderProperty4.setValue(isSmsOtpEnabled ? TRUE_STRING : FALSE_STRING);

        return new IdentityProviderProperty[]{
                identityProviderProperty1,
                identityProviderProperty2,
                identityProviderProperty3,
                identityProviderProperty4
        };
    }

    private HashMap<String, String> getAskPasswordConfigs(Boolean isEmailOtpEnabled, Boolean isSmsOtpEnabled) {

        HashMap<String, String> configs = new HashMap<>();
        if (isEmailOtpEnabled != null) {
            configs.put(ASK_PASSWORD_SEND_EMAIL_OTP,
                    isEmailOtpEnabled ? TRUE_STRING : FALSE_STRING);
        }
        if (isSmsOtpEnabled != null) {
            configs.put(ASK_PASSWORD_SEND_SMS_OTP,
                    isSmsOtpEnabled ? TRUE_STRING : FALSE_STRING);
        }
        return configs;
    }

    private IdentityProviderProperty[] getAskPasswordIdentityProviderProperties(
            boolean isEmailOtpEnabled, boolean isSmsOtpEnabled) {

        IdentityProviderProperty identityProviderProperty1 = new IdentityProviderProperty();
        identityProviderProperty1.setName(ASK_PASSWORD_SEND_EMAIL_OTP);
        identityProviderProperty1.setValue(isEmailOtpEnabled ? TRUE_STRING : FALSE_STRING);

        IdentityProviderProperty identityProviderProperty2 = new IdentityProviderProperty();
        identityProviderProperty2.setName(ASK_PASSWORD_SEND_SMS_OTP);
        identityProviderProperty2.setValue(isSmsOtpEnabled ? TRUE_STRING : FALSE_STRING);

        return new IdentityProviderProperty[]{
                identityProviderProperty1,
                identityProviderProperty2
        };
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

    @DataProvider
    public Object[][] getPreserveCurrentSessionAtPasswordUpdateData() {

        return new Object[][]{
                {"carbon.super", true, "true", true},
                {"test1", true, "false", false},
                {"test2", false, "true", true},
                {"test3", false, null, false},
        };
    }

    /**
     * Test getPreserveCurrentSessionAtPasswordUpdate method with various scenarios.
     */
    @Test(dataProvider = "getPreserveCurrentSessionAtPasswordUpdateData")
    public void testGetPreserveCurrentSessionAtPasswordUpdate(String tenantDomain, boolean defaultValue,
                                                             String propertyValue, boolean expectedResult)
            throws Exception {

        try (MockedStatic<IdentityProviderManager> identityProviderManager =
                     mockStatic(IdentityProviderManager.class);
             MockedStatic<IdentityApplicationManagementUtil> identityApplicationManagementUtil =
                     mockStatic(IdentityApplicationManagementUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {

            IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[0];

            identityUtil.when(() -> IdentityUtil.getProperty(PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE))
                    .thenReturn(String.valueOf(defaultValue));
            identityProviderManager.when(IdentityProviderManager::getInstance)
                    .thenReturn(mockedIdentityProviderManager);
            when(mockedIdentityProviderManager.getResidentIdP(tenantDomain)).thenReturn(mockedIdentityProvider);
            when(mockedIdentityProvider.getIdpProperties()).thenReturn(idpProperties);

            if (propertyValue != null) {
                identityApplicationManagementUtil.when(
                        () -> IdentityApplicationManagementUtil.getProperty(mockedIdentityProvider.getIdpProperties(),
                                PRESERVE_CURRENT_SESSION_AT_PASSWORD_UPDATE)).thenReturn(mockedIdentityProviderProperty);
                lenient().when(mockedIdentityProviderProperty.getValue()).thenReturn(propertyValue);
            } else {
                identityApplicationManagementUtil.when(
                        () -> IdentityApplicationManagementUtil.getProperty(mockedIdentityProvider.getIdpProperties(),
                                PRESERVE_CURRENT_SESSION_AT_PASSWORD_UPDATE)).thenReturn(null);
            }

            assertEquals(IdPManagementUtil.getPreserveCurrentSessionAtPasswordUpdate(tenantDomain), expectedResult);
        }
    }
}

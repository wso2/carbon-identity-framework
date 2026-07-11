/*
* Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.device.mgt.service;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.model.DeviceRegistrationInitiation;
import org.wso2.carbon.identity.device.mgt.internal.dao.DeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.service.impl.DeviceManagementServiceImpl;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DeviceManagementServiceImpl}.
 */
@WithCarbonHome
public class DeviceManagementServiceImplTest {

    private static final String TEST_USERNAME = "alice";
    private static final String TENANT_DOMAIN = "test.com";
    private static final int TENANT_ID = 1;

    private DeviceManagementServiceImpl service;
    private DeviceManagementDAO dao;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMocked;

    @BeforeClass
    public void setUpClass() {

        service = DeviceManagementServiceImpl.getInstance();
        identityTenantUtilMocked = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
    }

    @AfterClass
    public void tearDownClass() {

        identityTenantUtilMocked.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        dao = mock(DeviceManagementDAO.class);
        Field f = DeviceManagementServiceImpl.class.getDeclaredField("deviceManagementDAO");
        f.setAccessible(true);
        f.set(service, dao);
    }

    @Test
    public void testInitiateReturnsRegistrationIdAndChallenge() throws DeviceMgtException {

        DeviceRegistrationInitiation result = service.initiateDeviceRegistration(TEST_USERNAME, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getRegistrationId());
        Assert.assertFalse(result.getRegistrationId().isBlank());
        Assert.assertNotNull(result.getChallenge());
        Assert.assertFalse(result.getChallenge().isBlank());
    }

    @Test
    public void testInitiateGeneratesUniqueChallenges() throws DeviceMgtException {

        DeviceRegistrationInitiation r1 = service.initiateDeviceRegistration(TEST_USERNAME, TENANT_DOMAIN);
        DeviceRegistrationInitiation r2 = service.initiateDeviceRegistration(TEST_USERNAME, TENANT_DOMAIN);

        Assert.assertNotEquals(r1.getChallenge(), r2.getChallenge());
    }

    @Test
    public void testInitiateWithBlankUsernameThrows() {

        try {
            service.initiateDeviceRegistration("  ", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testInitiateWithBlankTenantDomainThrows() {

        try {
            service.initiateDeviceRegistration(TEST_USERNAME, "");
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testVerifyWithValidSignatureSucceeds() throws Exception {

        KeyPair kp = generateEcKeyPair();
        DeviceRegistrationInitiation initiation = service.initiateDeviceRegistration(TEST_USERNAME, TENANT_DOMAIN);
        String sig = signChallengeB64(kp, initiation.getChallenge());

        Device result = service.verifyDeviceRegistration(
                initiation.getRegistrationId(), publicKeyB64(kp), sig,
                "Alice's iPhone", null, null, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getPublicKey(), publicKeyB64(kp));
        Assert.assertEquals(result.getUserId(), TEST_USERNAME);
    }

    @Test
    public void testVerifyWithInvalidSignatureThrows() throws Exception {

        KeyPair kp1 = generateEcKeyPair();
        KeyPair kp2 = generateEcKeyPair();
        DeviceRegistrationInitiation initiation = service.initiateDeviceRegistration(TEST_USERNAME, TENANT_DOMAIN);
        String sigFromWrongKey = signChallengeB64(kp2, initiation.getChallenge());

        try {
            service.verifyDeviceRegistration(
                    initiation.getRegistrationId(), publicKeyB64(kp1), sigFromWrongKey,
                    "Alice's iPhone", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE.getCode());
        }
    }

    @Test
    public void testVerifyWithMissingRegistrationContextThrows() throws Exception {

        KeyPair kp = generateEcKeyPair();

        try {
            service.verifyDeviceRegistration(
                    UUID.randomUUID().toString(), publicKeyB64(kp), "fakeSig",
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testVerifyWithMalformedPublicKeyThrows() throws Exception {

        DeviceRegistrationInitiation initiation = service.initiateDeviceRegistration(TEST_USERNAME, TENANT_DOMAIN);
        String badKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});
        String fakeSig = Base64.getEncoder().encodeToString(new byte[]{0, 0, 0, 0});

        try {
            service.verifyDeviceRegistration(
                    initiation.getRegistrationId(), badKey, fakeSig,
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtServerException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_WHILE_VERIFYING_SIGNATURE.getCode());
        }
    }

    @Test
    public void testVerifyWithBlankRequiredFieldThrows() {

        try {
            service.verifyDeviceRegistration(
                    UUID.randomUUID().toString(), "", "sig",
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for blank publicKey");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }

        try {
            service.verifyDeviceRegistration(
                    UUID.randomUUID().toString(), "pk", "  ",
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for blank signature");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }

        try {
            service.verifyDeviceRegistration(
                    UUID.randomUUID().toString(), "pk", "sig",
                    "", null, null, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for blank deviceName");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testVerifyConsumesContext() throws Exception {

        KeyPair kp = generateEcKeyPair();
        DeviceRegistrationInitiation initiation = service.initiateDeviceRegistration(TEST_USERNAME, TENANT_DOMAIN);
        String sig = signChallengeB64(kp, initiation.getChallenge());

        service.verifyDeviceRegistration(
                initiation.getRegistrationId(), publicKeyB64(kp), sig,
                "Device", null, null, TENANT_DOMAIN);

        try {
            service.verifyDeviceRegistration(
                    initiation.getRegistrationId(), publicKeyB64(kp), sig,
                    "Device", null, null, TENANT_DOMAIN);
            Assert.fail("Expected context-not-found after context was consumed");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testCompleteVerifiesAndPersists() throws Exception {

        KeyPair kp = generateEcKeyPair();
        DeviceRegistrationInitiation initiation = service.initiateDeviceRegistration(TEST_USERNAME, TENANT_DOMAIN);
        String sig = signChallengeB64(kp, initiation.getChallenge());

        Device verified = service.verifyDeviceRegistration(
                initiation.getRegistrationId(), publicKeyB64(kp), sig,
                "Alice's iPhone", "iPhone 15", null, TENANT_DOMAIN);

        when(dao.registerDevice(any(), eq(TENANT_ID))).thenReturn(verified);
        service.persistDevice(verified, TENANT_DOMAIN);

        verify(dao).registerDevice(any(), eq(TENANT_ID));
    }

    @Test
    public void testGetDeviceByIdDelegatesToDao() throws Exception {

        Device device = mock(Device.class);
        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(device);

        Device result = service.getDeviceById("d1", TENANT_DOMAIN);

        Assert.assertEquals(result, device);
    }

    @Test
    public void testGetDeviceByIdWithBlankIdThrows() {

        try {
            service.getDeviceById("", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testUpdateDeviceNameWhenDeviceMissingThrows() throws Exception {

        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(null);

        try {
            service.updateDeviceName("d1", "New Name", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testUpdateDeviceNameWithBlankArgsThrows() {

        try {
            service.updateDeviceName("", "New Name", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for blank deviceId");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }

        try {
            service.updateDeviceName("d1", "  ", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for blank deviceName");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testDeleteDeviceWhenDeviceMissingThrows() throws Exception {

        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(null);

        try {
            service.deleteDevice("d1", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testDeleteDeviceDelegatesToDao() throws Exception {

        Device device = mock(Device.class);
        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(device);

        service.deleteDevice("d1", TENANT_DOMAIN);

        verify(dao).deleteDevice("d1", TENANT_ID);
    }

    @Test
    public void testGetDevicesByUserIdWithBlankUserThrows() {

        try {
            service.getDevicesByUserId("", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    private static KeyPair generateEcKeyPair() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        return kpg.generateKeyPair();
    }

    private static String publicKeyB64(KeyPair kp) {

        return Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
    }

    private static String signChallengeB64(KeyPair kp, String challengeB64Url) throws Exception {

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initSign(kp.getPrivate());
        sig.update(Base64.getUrlDecoder().decode(challengeB64Url));
        return Base64.getEncoder().encodeToString(sig.sign());
    }
}

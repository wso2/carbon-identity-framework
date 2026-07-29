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

package org.wso2.carbon.identity.device.mgt.util;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.model.DeviceAssociation;
import org.wso2.carbon.identity.device.mgt.api.model.UserDeviceAssociation;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceValidator;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Unit tests for {@link DeviceValidator}.
 */
@WithCarbonHome
public class DeviceValidatorTest {

    private final DeviceValidator deviceValidator = new DeviceValidator();

    @Test
    public void testValidateRequiredFieldWithBlankValueThrows() {

        try {
            deviceValidator.validateRequiredField("  ", "deviceId");
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testValidateRequiredFieldWithNullValueThrows() {

        try {
            deviceValidator.validateRequiredField(null, "deviceId");
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testValidateRequiredFieldWithValidValueSucceeds() throws Exception {

        deviceValidator.validateRequiredField("d1", "deviceId");
    }

    @Test
    public void testValidateDeviceForRegistrationWithNullDeviceThrows() {

        try {
            deviceValidator.validateDeviceForRegistration(null);
            Assert.fail("Expected DeviceMgtServerException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_FIELD_REQUIRED.getCode());
        }
    }

    @Test
    public void testValidateAssociationWithNullAssociationThrows() {

        try {
            deviceValidator.validateAssociation(null);
            Assert.fail("Expected DeviceMgtServerException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_ASSOCIATION.getCode());
        }
    }

    @Test
    public void testValidateAssociationWithNonUserDeviceAssociationThrows() {

        DeviceAssociation customAssociation = new DeviceAssociation("d1") { };
        try {
            deviceValidator.validateAssociation(customAssociation);
            Assert.fail("Expected DeviceMgtServerException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_ASSOCIATION.getCode());
        }
    }

    @Test
    public void testValidateAssociationWithBlankUserIdThrows() {

        try {
            deviceValidator.validateAssociation(new UserDeviceAssociation("d1", ""));
            Assert.fail("Expected DeviceMgtServerException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_ASSOCIATION.getCode());
        }
    }

    @Test
    public void testValidateAssociationWithValidAssociationSucceeds() throws Exception {

        deviceValidator.validateAssociation(new UserDeviceAssociation("d1", "alice@example.com"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidateDeviceForRegistrationWithoutIdThrows() {

        completeDeviceBuilder().id(null).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidateDeviceForRegistrationWithoutDeviceNameThrows() {

        completeDeviceBuilder().deviceName(null).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValidateDeviceForRegistrationWithoutPublicKeyThrows() {

        completeDeviceBuilder().publicKey(null).build();
    }

    @Test
    public void testValidateDeviceForRegistrationWithoutOptionalFieldsSucceeds() throws Exception {

        Device device = completeDeviceBuilder().deviceModel(null).build();

        deviceValidator.validateDeviceForRegistration(device);
    }

    @Test
    public void testValidateRequiredDeviceFieldWithBlankValueThrows() {

        try {
            deviceValidator.validateRequiredDeviceField("", "id");
            Assert.fail("Expected DeviceMgtException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_FIELD_REQUIRED.getCode());
        }
    }

    @Test
    public void testValidateRequiredDeviceFieldWithValidValueSucceeds() throws Exception {

        deviceValidator.validateRequiredDeviceField("d1", "id");
    }

    @Test
    public void testValidateLimitWithNegativeValueThrows() {

        try {
            deviceValidator.validateLimit(-1);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testValidateLimitWithinBoundsIsUnchanged() throws Exception {

        int result = deviceValidator.validateLimit(20);

        Assert.assertEquals(result, 20);
    }

    @Test
    public void testValidateLimitOverMaximumIsCapped() throws Exception {

        int maximumItemsPerPage = IdentityUtil.getMaximumItemPerPage();

        int result = deviceValidator.validateLimit(maximumItemsPerPage + 5000);

        Assert.assertEquals(result, maximumItemsPerPage);
    }

    @Test
    public void testValidateDeviceExistsWithNullDeviceThrows() {

        try {
            deviceValidator.validateDeviceExists(null, "d1");
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testValidateDeviceExistsWithExistingDeviceSucceeds() throws Exception {

        Device device = completeDeviceBuilder().build();

        deviceValidator.validateDeviceExists(device, "d1");
    }

    private static Device.Builder completeDeviceBuilder() {

        return new Device.Builder()
                .id("d1")
                .deviceName("Alice's iPhone")
                .deviceModel("iPhone 15")
                .publicKey("dummy-public-key")
                .status(Device.Status.ACTIVE)
                .registeredAt(Timestamp.from(Instant.now()));
    }
}

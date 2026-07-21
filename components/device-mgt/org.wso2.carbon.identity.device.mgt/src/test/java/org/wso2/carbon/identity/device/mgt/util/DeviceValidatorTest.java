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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
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
    public void testValidateDeviceForRegistrationWithoutUserIdThrows() {

        Device device = completeDeviceBuilder().userId(null).build();

        try {
            deviceValidator.validateDeviceForRegistration(device);
            Assert.fail("Expected DeviceMgtServerException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_USER_ID_REQUIRED.getCode());
        }
    }

    @DataProvider(name = "incompleteDevices")
    public Object[][] incompleteDevices() {

        return new Object[][]{
                {completeDeviceBuilder().id(null).build()},
                {completeDeviceBuilder().deviceName(null).build()},
                {completeDeviceBuilder().publicKey(null).build()},
                {completeDeviceBuilder().status(null).build()},
                {completeDeviceBuilder().registeredAt(null).build()},
        };
    }

    @Test(dataProvider = "incompleteDevices")
    public void testValidateDeviceForRegistrationWithMissingRequiredFieldThrows(Device device) {

        try {
            deviceValidator.validateDeviceForRegistration(device);
            Assert.fail("Expected DeviceMgtServerException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_FIELD_REQUIRED.getCode());
        }
    }

    @Test
    public void testValidateDeviceForRegistrationWithoutOptionalFieldsSucceeds() throws Exception {

        Device device = completeDeviceBuilder().deviceModel(null).metadata(null).build();

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
                .userId("alice@example.com")
                .deviceName("Alice's iPhone")
                .deviceModel("iPhone 15")
                .publicKey("dummy-public-key")
                .status(Device.Status.ACTIVE)
                .registeredAt(Timestamp.from(Instant.now()));
    }
}

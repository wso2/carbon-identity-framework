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
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtClientException;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtServerException;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceManagementExceptionHandler;

/**
 * Unit tests for {@link DeviceManagementExceptionHandler}.
 */
public class DeviceManagementExceptionHandlerTest {

    @Test
    public void testHandleClientException() {

        ErrorMessage error = ErrorMessage.ERROR_DEVICE_NOT_FOUND;
        DeviceMgtClientException ex = DeviceManagementExceptionHandler.handleClientException(error, "device123");

        Assert.assertEquals(ex.getErrorCode(), error.getCode());
        Assert.assertTrue(ex.getDescription().contains("device123"));
        Assert.assertNull(ex.getCause());
    }

    @Test
    public void testHandleClientExceptionWithCause() {

        ErrorMessage error = ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE;
        Throwable cause = new RuntimeException("sig error");
        DeviceMgtClientException ex = DeviceManagementExceptionHandler.handleClientException(error, cause, "reg123");

        Assert.assertEquals(ex.getErrorCode(), error.getCode());
        Assert.assertTrue(ex.getDescription().contains("reg123"));
        Assert.assertEquals(ex.getCause(), cause);
    }

    @Test
    public void testHandleServerExceptionWithCause() {

        ErrorMessage error = ErrorMessage.ERROR_WHILE_VERIFYING_SIGNATURE;
        Throwable cause = new RuntimeException("verify error");
        DeviceMgtServerException ex = DeviceManagementExceptionHandler.handleServerException(error, cause, "reg456");

        Assert.assertEquals(ex.getErrorCode(), error.getCode());
        Assert.assertTrue(ex.getDescription().contains("reg456"));
        Assert.assertEquals(ex.getCause(), cause);
    }

    @Test
    public void testHandleServerExceptionNoCause() {

        ErrorMessage error = ErrorMessage.ERROR_WHILE_REGISTERING_DEVICE;
        DeviceMgtServerException ex = DeviceManagementExceptionHandler.handleServerException(error);

        Assert.assertEquals(ex.getErrorCode(), error.getCode());
        Assert.assertNotNull(ex.getDescription());
        Assert.assertNull(ex.getCause());
    }

    @Test
    public void testDescriptionSubstitutionFormatting() {

        ErrorMessage error = ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND;
        String regId = "test-reg-id-999";
        DeviceMgtClientException ex = DeviceManagementExceptionHandler.handleClientException(error, regId);

        Assert.assertEquals(ex.getErrorCode(), error.getCode());
        Assert.assertTrue(ex.getDescription().contains(regId),
                "Description should contain substituted value: " + regId);
    }

    @Test
    public void testMessageIsPreserved() {

        ErrorMessage error = ErrorMessage.ERROR_DEVICE_NOT_FOUND;
        DeviceMgtClientException ex = DeviceManagementExceptionHandler.handleClientException(error, "d1");

        Assert.assertEquals(ex.getMessage(), error.getMessage());
    }
}

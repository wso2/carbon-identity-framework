/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.registration.internal.util;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.registration.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.device.registration.internal.exception.DeviceRegistrationException;

/**
 * Unit tests for {@link DeviceRegistrationExceptionHandler}.
 */
public class DeviceRegistrationExceptionHandlerTest {

    @Test
    public void testHandleClientException() {

        ErrorMessage error = ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND;
        DeviceRegistrationException ex = DeviceRegistrationExceptionHandler.handleClientException(error, "reg-123");

        Assert.assertEquals(ex.getErrorCode(), error.getCode());
        Assert.assertEquals(ex.getMessage(), error.getMessage());
        Assert.assertTrue(ex.getDescription().contains("reg-123"));
        Assert.assertNull(ex.getCause());
    }

    @Test
    public void testHandleClientExceptionWithCause() {

        ErrorMessage error = ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE;
        Throwable cause = new RuntimeException("signature error");
        DeviceRegistrationException ex =
                DeviceRegistrationExceptionHandler.handleClientException(error, cause, "reg-456");

        Assert.assertEquals(ex.getErrorCode(), error.getCode());
        Assert.assertEquals(ex.getMessage(), error.getMessage());
        Assert.assertTrue(ex.getDescription().contains("reg-456"));
        Assert.assertEquals(ex.getCause(), cause);
    }

    @Test
    public void testHandleServerException() {

        ErrorMessage error = ErrorMessage.ERROR_WHILE_VERIFYING_SIGNATURE;
        DeviceRegistrationException ex = DeviceRegistrationExceptionHandler.handleServerException(error, "reg-789");

        Assert.assertEquals(ex.getErrorCode(), error.getCode());
        Assert.assertEquals(ex.getMessage(), error.getMessage());
        Assert.assertTrue(ex.getDescription().contains("reg-789"));
        Assert.assertNull(ex.getCause());
    }

    @Test
    public void testHandleServerExceptionWithCause() {

        ErrorMessage error = ErrorMessage.ERROR_WHILE_EVALUATING_POLICY;
        Throwable cause = new RuntimeException("evaluation error");
        DeviceRegistrationException ex =
                DeviceRegistrationExceptionHandler.handleServerException(error, cause, "strictPolicy");

        Assert.assertEquals(ex.getErrorCode(), error.getCode());
        Assert.assertEquals(ex.getMessage(), error.getMessage());
        Assert.assertTrue(ex.getDescription().contains("strictPolicy"));
        Assert.assertEquals(ex.getCause(), cause);
    }

    @Test
    public void testDescriptionPlaceholderFormattingWithMultipleValues() {

        ErrorMessage error = ErrorMessage.ERROR_DEVICE_POLICY_NOT_COMPLIANT;
        DeviceRegistrationException ex = DeviceRegistrationExceptionHandler.handleClientException(
                error, "strictPolicy", "osVersion,imei");

        Assert.assertTrue(ex.getDescription().contains("strictPolicy"));
        Assert.assertTrue(ex.getDescription().contains("osVersion,imei"));
    }

    @Test
    public void testDescriptionWithNoPlaceholderDataLeavesDescriptionUnformatted() {

        ErrorMessage error = ErrorMessage.ERROR_USER_NOT_IDENTIFIED;
        DeviceRegistrationException ex = DeviceRegistrationExceptionHandler.handleClientException(error);

        Assert.assertEquals(ex.getDescription(), error.getDescription());
    }
}

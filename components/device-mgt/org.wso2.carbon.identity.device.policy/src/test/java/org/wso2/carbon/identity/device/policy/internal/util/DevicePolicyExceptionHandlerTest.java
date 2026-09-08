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

package org.wso2.carbon.identity.device.policy.internal.util;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for DevicePolicyExceptionHandler.
 */
public class DevicePolicyExceptionHandlerTest {

    @Test
    public void testHandleClientException() {

        DevicePolicyClientException exception = DevicePolicyExceptionHandler
                .handleClientException(DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE);
        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE.getCode());
        assertEquals(exception.getMessage(), DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE.getMessage());
        assertEquals(exception.getDescription(), DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE.getDescription());
    }

    @Test
    public void testHandleClientExceptionWithData() {

        String testData = "device-123";
        DevicePolicyClientException exception = DevicePolicyExceptionHandler
                .handleClientException(DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE, testData);
        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE.getCode());
        assertTrue(exception.getDescription().contains(testData));
    }

    @Test
    public void testHandleClientExceptionWithThrowable() {

        Throwable cause = new Throwable("Test cause");
        DevicePolicyClientException exception = DevicePolicyExceptionHandler
                .handleClientException(DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE, cause);
        assertNotNull(exception);
        assertEquals(exception.getCause(), cause);
        assertEquals(exception.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE.getCode());
    }

    @Test
    public void testHandleClientExceptionWithThrowableAndData() {

        Throwable cause = new Throwable("Test cause");
        String testData = "device-123";
        DevicePolicyClientException exception = DevicePolicyExceptionHandler
                .handleClientException(DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE, cause, testData);
        assertNotNull(exception);
        assertEquals(exception.getCause(), cause);
        assertEquals(exception.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_NOT_ACTIVE.getCode());
        assertTrue(exception.getDescription().contains(testData));
    }

    @Test
    public void testHandleServerException() {

        DevicePolicyServerException exception = DevicePolicyExceptionHandler
                .handleServerException(DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED);
        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED.getCode());
        assertEquals(exception.getMessage(), DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED.getMessage());
        assertEquals(exception.getDescription(), DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED.getDescription());
    }

    @Test
    public void testHandleServerExceptionWithData() {

        String testData = "device-123";
        DevicePolicyServerException exception = DevicePolicyExceptionHandler
                .handleServerException(DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED, testData);
        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED.getCode());
    }

    @Test
    public void testHandleServerExceptionWithThrowable() {

        Throwable cause = new Throwable("Test cause");
        DevicePolicyServerException exception = DevicePolicyExceptionHandler
                .handleServerException(DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED, cause);
        assertNotNull(exception);
        assertEquals(exception.getCause(), cause);
        assertEquals(exception.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED.getCode());
    }

    @Test
    public void testHandleServerExceptionWithThrowableAndData() {

        Throwable cause = new Throwable("Test cause");
        String testData = "device-123";
        DevicePolicyServerException exception = DevicePolicyExceptionHandler
                .handleServerException(DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED, cause, testData);
        assertNotNull(exception);
        assertEquals(exception.getCause(), cause);
        assertEquals(exception.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_LOOKUP_FAILED.getCode());
    }
}

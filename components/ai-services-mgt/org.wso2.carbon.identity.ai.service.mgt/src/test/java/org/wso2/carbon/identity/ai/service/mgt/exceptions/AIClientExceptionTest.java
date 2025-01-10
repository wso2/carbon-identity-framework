/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.ai.service.mgt.exceptions;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/**
 * Test class for AIClientException.
 */
public class AIClientExceptionTest {

    @Test
    public void testAIClientExceptionWithMessageAndErrorCode() {

        AIClientException exception = new AIClientException("Test message", "AI_10001");
        assertEquals("Test message", exception.getMessage());
        assertEquals("AI_10001", exception.getErrorCode());
        assertNull(exception.getServerMessage());
    }

    @Test
    public void testAIClientExceptionWithHttpResponseWrapperMessageAndErrorCode() {

        int statusCode = 400;
        String message = "Test message";
        AIClientException exception = new AIClientException("Test message", "AI_10002", statusCode, message);
        assertEquals("Test message", exception.getMessage());
        assertEquals("AI_10002", exception.getErrorCode());
        assertEquals(statusCode, exception.getServerStatusCode());
        assertEquals(message, exception.getServerMessage());
    }

    @Test
    public void testAIClientExceptionWithMessageErrorCodeAndCause() {
        Throwable cause = new Throwable("Cause message");
        AIClientException exception = new AIClientException("Test message", "AI_10003", cause);
        assertEquals("Test message", exception.getMessage());
        assertEquals("AI_10003", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }
}

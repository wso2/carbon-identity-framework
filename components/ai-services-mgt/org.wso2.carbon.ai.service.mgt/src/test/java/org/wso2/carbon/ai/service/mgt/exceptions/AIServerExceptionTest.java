/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.ai.service.mgt.exceptions;

import org.testng.annotations.Test;
import org.wso2.carbon.ai.service.mgt.util.AIHttpClientUtil;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/**
 * Test class for AIServerException.
 */
public class AIServerExceptionTest {

    @Test
    public void testAIServerExceptionWithMessageAndErrorCode() {

        AIServerException exception = new AIServerException("Test message", "AI_20001");
        assertEquals("Test message", exception.getMessage());
        assertEquals("AI_20001", exception.getErrorCode());
        assertNull(exception.getBrandingAIResponse());
    }

    @Test
    public void testAIServerExceptionWithHttpResponseWrapperMessageAndErrorCode() {

        AIHttpClientUtil.HttpResponseWrapper responseWrapper = new AIHttpClientUtil.HttpResponseWrapper(
                500, "Test response");
        AIServerException exception = new AIServerException(responseWrapper, "Test message", "AI_20002");
        assertEquals("Test message", exception.getMessage());
        assertEquals("AI_20002", exception.getErrorCode());
        assertEquals(responseWrapper, exception.getBrandingAIResponse());
    }

    @Test
    public void testAIServerExceptionWithMessageAndCause() {
        Throwable cause = new Throwable("Cause message");
        AIServerException exception = new AIServerException("Test message", cause);
        assertEquals("Test message", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getBrandingAIResponse());
    }

    @Test
    public void testAIServerExceptionWithMessageErrorCodeAndCause() {

        Throwable cause = new Throwable("Cause message");
        AIServerException exception = new AIServerException("Test message", "AI_20003", cause);
        assertEquals("Test message", exception.getMessage());
        assertEquals("AI_20003", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getBrandingAIResponse());
    }
}

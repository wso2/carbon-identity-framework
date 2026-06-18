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

package org.wso2.carbon.identity.application.authentication.framework.exception;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Unit tests for {@link ConsentAppMappingException}.
 */
public class ConsentAppMappingExceptionTest {

    @Test
    public void testConstructorWithMessage() {

        ConsentAppMappingException ex = new ConsentAppMappingException("test message");
        assertEquals(ex.getMessage(), "test message");
    }

    @Test
    public void testConstructorWithErrorCodeAndMessage() {

        ConsentAppMappingException ex = new ConsentAppMappingException("ERR-001", "error occurred");
        assertEquals(ex.getErrorCode(), "ERR-001");
        assertEquals(ex.getMessage(), "error occurred");
    }

    @Test
    public void testConstructorWithMessageAndCause() {

        Throwable cause = new RuntimeException("root cause");
        ConsentAppMappingException ex = new ConsentAppMappingException("test message", cause);
        assertEquals(ex.getMessage(), "test message");
        assertSame(ex.getCause(), cause);
    }

    @Test
    public void testConstructorWithErrorCodeMessageAndCause() {

        Throwable cause = new RuntimeException("root cause");
        ConsentAppMappingException ex = new ConsentAppMappingException("ERR-002", "error occurred", cause);
        assertEquals(ex.getErrorCode(), "ERR-002");
        assertEquals(ex.getMessage(), "error occurred");
        assertSame(ex.getCause(), cause);
    }
}

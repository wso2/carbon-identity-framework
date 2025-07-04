/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
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

package org.wso2.carbon.identity.event.publisher.exception;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherServerException;

public class EventPublisherServerExceptionTest {

    @Test
    public void testConstructorWithoutCause() {

        String errorCode = "SERVER_ERR_001";
        String message = "Server error occurred";
        String description = "A server-side error happened";

        EventPublisherServerException exception =
                new EventPublisherServerException(errorCode, message, description);

        Assert.assertEquals(exception.getErrorCode(), errorCode);
        Assert.assertEquals(exception.getMessage(), message);
        Assert.assertEquals(exception.getDescription(), description);
        Assert.assertNull(exception.getCause());
    }

    @Test
    public void testConstructorWithCause() {

        String errorCode = "SERVER_ERR_002";
        String message = "Another server error";
        String description = "Another server-side error";
        Throwable cause = new RuntimeException("Root cause");

        EventPublisherServerException exception =
                new EventPublisherServerException(errorCode, message, description, cause);

        Assert.assertEquals(exception.getErrorCode(), errorCode);
        Assert.assertEquals(exception.getMessage(), message);
        Assert.assertEquals(exception.getDescription(), description);
        Assert.assertEquals(exception.getCause(), cause);
    }
}

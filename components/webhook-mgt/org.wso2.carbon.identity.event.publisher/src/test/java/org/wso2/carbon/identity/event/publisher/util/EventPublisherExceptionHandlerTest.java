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

package org.wso2.carbon.identity.event.publisher.util;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.event.publisher.api.constant.ErrorMessage;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherClientException;
import org.wso2.carbon.identity.event.publisher.api.exception.EventPublisherServerException;
import org.wso2.carbon.identity.event.publisher.internal.util.EventPublisherExceptionHandler;

public class EventPublisherExceptionHandlerTest {

    @Test
    public void testHandleClientExceptionWithoutData() {

        ErrorMessage error = Mockito.mock(ErrorMessage.class);
        Mockito.when(error.getCode()).thenReturn("CLIENT_001");
        Mockito.when(error.getMessage()).thenReturn("Client error");
        Mockito.when(error.getDescription()).thenReturn("Description");

        EventPublisherClientException ex =
                EventPublisherExceptionHandler.handleClientException(error);

        Assert.assertEquals(ex.getErrorCode(), "CLIENT_001");
        Assert.assertEquals(ex.getMessage(), "Client error");
        Assert.assertEquals(ex.getDescription(), "Description");
    }

    @Test
    public void testHandleClientExceptionWithData() {

        ErrorMessage error = Mockito.mock(ErrorMessage.class);
        Mockito.when(error.getCode()).thenReturn("CLIENT_002");
        Mockito.when(error.getMessage()).thenReturn("Client error");
        Mockito.when(error.getDescription()).thenReturn("Description: %s");

        EventPublisherClientException ex =
                EventPublisherExceptionHandler.handleClientException(error, "foo");

        Assert.assertEquals(ex.getErrorCode(), "CLIENT_002");
        Assert.assertEquals(ex.getMessage(), "Client error");
        Assert.assertEquals(ex.getDescription(), "Description: foo");
    }

    @Test
    public void testHandleServerExceptionWithoutThrowable() {

        ErrorMessage error = Mockito.mock(ErrorMessage.class);
        Mockito.when(error.getCode()).thenReturn("SERVER_001");
        Mockito.when(error.getMessage()).thenReturn("Server error");
        Mockito.when(error.getDescription()).thenReturn("Server description");

        EventPublisherServerException ex =
                EventPublisherExceptionHandler.handleServerException(error);

        Assert.assertEquals(ex.getErrorCode(), "SERVER_001");
        Assert.assertEquals(ex.getMessage(), "Server error");
        Assert.assertEquals(ex.getDescription(), "Server description");
        Assert.assertNull(ex.getCause());
    }

    @Test
    public void testHandleServerExceptionWithData() {

        ErrorMessage error = Mockito.mock(ErrorMessage.class);
        Mockito.when(error.getCode()).thenReturn("SERVER_002");
        Mockito.when(error.getMessage()).thenReturn("Server error");
        Mockito.when(error.getDescription()).thenReturn("Server: %s");

        EventPublisherServerException ex =
                EventPublisherExceptionHandler.handleServerException(error, "bar");

        Assert.assertEquals(ex.getErrorCode(), "SERVER_002");
        Assert.assertEquals(ex.getMessage(), "Server error");
        Assert.assertEquals(ex.getDescription(), "Server: bar");
        Assert.assertNull(ex.getCause());
    }

    @Test
    public void testHandleServerExceptionWithThrowableAndData() {

        ErrorMessage error = Mockito.mock(ErrorMessage.class);
        Mockito.when(error.getCode()).thenReturn("SERVER_003");
        Mockito.when(error.getMessage()).thenReturn("Server error");
        Mockito.when(error.getDescription()).thenReturn("Server: %s");

        Throwable cause = new RuntimeException("Root cause");

        EventPublisherServerException ex =
                EventPublisherExceptionHandler.handleServerException(error, cause, "baz");

        Assert.assertEquals(ex.getErrorCode(), "SERVER_003");
        Assert.assertEquals(ex.getMessage(), "Server error");
        Assert.assertEquals(ex.getDescription(), "Server: baz");
        Assert.assertEquals(ex.getCause(), cause);
    }
}

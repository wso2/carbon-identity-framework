/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.session.extender.response;

import org.powermock.modules.testng.PowerMockTestCase;
import org.slf4j.MDC;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.exception.SessionExtenderClientException;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.exception.SessionExtenderServerException;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.ERROR_RESPONSE_BODY;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.EXCEPTION_ERROR_CODE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.EXCEPTION_MESSAGE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.TRACE_ID;

/**
 * Unit test cases for SessionExtenderResponseFactory.
 */
public class SessionExtenderResponseFactoryTest extends PowerMockTestCase {

    private SessionExtenderResponseFactory sessionExtenderResponseFactory;

    @BeforeMethod
    public void setUp() throws Exception {
        sessionExtenderResponseFactory = new SessionExtenderResponseFactory();
    }

    @Test
    public void testCanHandleResponse() {

        SessionExtenderResponse sessionExtenderResponse = mock(SessionExtenderResponse.class);
        assertTrue(sessionExtenderResponseFactory.canHandle(sessionExtenderResponse),
                "Cannot handle valid response");
    }

    @DataProvider(name = "canHandleExceptionProvider")
    public Object[][] getExceptionsForCanHandle() {

        return new Object[][] {
                {new SessionExtenderClientException(EXCEPTION_MESSAGE)},
                {new SessionExtenderServerException(EXCEPTION_MESSAGE)}
        };
    }

    @Test(dataProvider = "canHandleExceptionProvider")
    public void testCanHandleExceptions(FrameworkException exception) {

        assertTrue(sessionExtenderResponseFactory.canHandle(exception), "Cannot handle valid exception.");
    }

    @Test
    public void testCreate() {

        SessionExtenderResponse sessionExtenderResponse = mock(SessionExtenderResponse.class);
        when(sessionExtenderResponse.getTraceId()).thenReturn(TRACE_ID);

        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder =
                sessionExtenderResponseFactory.create(sessionExtenderResponse);
        HttpIdentityResponse response = responseBuilder.build();
        assertNull(response.getBody());
        assertNotNull(response.getHeaders().get("Trace-ID"));
    }

    @DataProvider(name = "handleExceptionProvider")
    public Object[][] getExceptionsForHandle() {

        return new Object[][] {
                {new SessionExtenderClientException(EXCEPTION_ERROR_CODE, EXCEPTION_MESSAGE), 400},
                {new SessionExtenderServerException(EXCEPTION_ERROR_CODE, EXCEPTION_MESSAGE), 500}
        };
    }

    @Test(dataProvider = "handleExceptionProvider")
    public void testHandleExceptions(FrameworkException exception, int expectedStatusCode) {

        MDC.put("Correlation-ID", TRACE_ID);
        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder =
                sessionExtenderResponseFactory.handleException(exception);
        HttpIdentityResponse response = responseBuilder.build();

        assertEquals(response.getBody(), ERROR_RESPONSE_BODY);
        assertEquals(response.getStatusCode(), expectedStatusCode);
    }
}

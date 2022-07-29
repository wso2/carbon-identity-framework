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

package org.wso2.carbon.identity.application.authentication.framework.session.extender.request;

import org.mockito.Mock;
import org.slf4j.MDC;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkClientException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.exception.SessionExtenderClientException;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderConstants.SESSION_ID_PARAM_NAME;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.ERROR_RESPONSE_BODY;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.EXCEPTION_DESCRIPTION;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.EXCEPTION_ERROR_CODE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.EXCEPTION_MESSAGE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.IDP_SESSION_KEY;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.SESSION_COOKIE_NAME;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.SESSION_COOKIE_VALUE;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.SESSION_EXTENDER_ENDPOINT_URL;
import static org.wso2.carbon.identity.application.authentication.framework.session.extender.SessionExtenderTestConstants.TRACE_ID;

/**
 * Unit test cases for SessionExtenderRequestFactory.
 */
public class SessionExtenderRequestFactoryTest {

    @Mock
    private HttpServletRequest mockedHttpRequest;

    @Mock
    private HttpServletResponse mockedHttpResponse;

    @Mock
    private Enumeration<String> mockedList;

    private SessionExtenderRequestFactory sessionExtenderRequestFactory;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        sessionExtenderRequestFactory = new SessionExtenderRequestFactory();
        CommonTestUtils.initPrivilegedCarbonContext();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        PrivilegedCarbonContext.endTenantFlow();
    }

    @DataProvider(name = "urlProvider")
    public Object[][] getURLs() {

        return new Object[][]{
                {SESSION_EXTENDER_ENDPOINT_URL, true},
                {"http://test-url.com/", false}
        };
    }

    @Test(dataProvider = "urlProvider")
    public void testCanHandle(String requestURI, boolean canHandleResult) {

        when(mockedHttpRequest.getRequestURI()).thenReturn(requestURI);
        assertEquals(sessionExtenderRequestFactory.canHandle(mockedHttpRequest, mockedHttpResponse), canHandleResult,
                "Failed to handle valid request.");
    }

    @DataProvider(name = "requestDataProvider")
    public Object[][] getRequestData() {

        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, SESSION_COOKIE_VALUE);
        return new Object[][]{
                {IDP_SESSION_KEY, null, IDP_SESSION_KEY, null},
                {null, new Cookie[] {sessionCookie}, null, sessionCookie},
                {IDP_SESSION_KEY, new Cookie[] {sessionCookie}, IDP_SESSION_KEY, sessionCookie}
        };
    }

    @Test(dataProvider = "requestDataProvider")
    public void testCreate(String idpSessionKey, Cookie[] requestCookies, String expectedSessionKey,
                           Cookie expectedCookie) throws Exception {

        when(mockedHttpRequest.getParameter(SESSION_ID_PARAM_NAME)).thenReturn(idpSessionKey);
        when(mockedHttpRequest.getCookies()).thenReturn(requestCookies);
        when(mockedHttpRequest.getHeaderNames()).thenReturn(mockedList);
        when(mockedHttpRequest.getAttributeNames()).thenReturn(mockedList);

        SessionExtenderRequest.SessionExtenderRequestBuilder requestBuilder =
                (SessionExtenderRequest.SessionExtenderRequestBuilder) sessionExtenderRequestFactory.create(
                        mockedHttpRequest, mockedHttpResponse);
        SessionExtenderRequest request = requestBuilder.build();

        assertEquals(request.getSessionKey(), expectedSessionKey, "Session Key not being set properly.");
        assertEquals(request.getSessionCookie(), expectedCookie,
                "Session cookie included in Session Id only request.");
    }

    @Test(expectedExceptions = SessionExtenderClientException.class)
    public void testFailCreate() throws FrameworkClientException {

        when(mockedHttpRequest.getParameter(SESSION_ID_PARAM_NAME)).thenReturn(null);
        when(mockedHttpRequest.getCookies()).thenReturn(new Cookie[] {});
        when(mockedHttpRequest.getHeaderNames()).thenReturn(mockedList);
        when(mockedHttpRequest.getAttributeNames()).thenReturn(mockedList);

        sessionExtenderRequestFactory.create(mockedHttpRequest, mockedHttpResponse);
    }

    @DataProvider(name = "handleExceptionProvider")
    public Object[][] getExceptionsForHandle() {

        return new Object[][] {
                {new SessionExtenderClientException(EXCEPTION_ERROR_CODE, EXCEPTION_MESSAGE, EXCEPTION_DESCRIPTION),
                        400}
        };
    }

    @Test(dataProvider = "handleExceptionProvider")
    public void testHandleException(FrameworkClientException exception, int expectedStatusCode) {

        MDC.put("Correlation-ID", TRACE_ID);
        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder =
                sessionExtenderRequestFactory.handleException(exception, mockedHttpRequest, mockedHttpResponse);
        HttpIdentityResponse response = responseBuilder.build();
        assertEquals(response.getBody(), ERROR_RESPONSE_BODY);
        assertEquals(response.getStatusCode(), expectedStatusCode);
    }
}

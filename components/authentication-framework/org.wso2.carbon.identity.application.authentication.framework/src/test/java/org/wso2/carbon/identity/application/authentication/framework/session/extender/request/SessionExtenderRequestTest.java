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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test cases for SessionExtenderRequest.
 */
public class SessionExtenderRequestTest {

    @Mock
    private HttpServletRequest mockedHttpRequest;

    @Mock
    private HttpServletResponse mockedHttpResponse;

    @Mock
    private Cookie sessionCookie;

    @BeforeMethod
    public void setUp() {

        initMocks(this);
    }

    @Test
    public void buildTestRequestWithSessionIdParam() throws Exception {

        SessionExtenderRequest.SessionExtenderRequestBuilder builder =
                new SessionExtenderRequest.SessionExtenderRequestBuilder(mockedHttpRequest, mockedHttpResponse);
        builder.setSessionKey("Random string");
        SessionExtenderRequest request = builder.build();
        assertNotNull(request.getSessionKey(), "Failed to set session identifier key to request.");
    }

    @Test
    public void buildTestRequestWithSessionIdCookie() throws Exception {

        SessionExtenderRequest.SessionExtenderRequestBuilder builder =
                new SessionExtenderRequest.SessionExtenderRequestBuilder(mockedHttpRequest, mockedHttpResponse);
        builder.setSessionCookie(sessionCookie);
        SessionExtenderRequest request = builder.build();
        assertNotNull(request.getSessionCookie(), "Failed to set session identifier cookie to request.");
    }
}

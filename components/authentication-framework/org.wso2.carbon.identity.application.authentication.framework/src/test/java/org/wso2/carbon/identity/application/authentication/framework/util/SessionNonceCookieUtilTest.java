/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.util;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.NONCE_COOKIE;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.NONCE_COOKIE_CONFIG;

@PrepareForTest({IdentityUtil.class})
public class SessionNonceCookieUtilTest {

    private static final String OLD_SESSION_DATA_KEY_VALUE = "oldSessionDataKeyValue";
    private static final String NEW_SESSION_DATA_KEY_VALUE = "newSessionDataKeyValue";
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationContext context;

    @Captor
    ArgumentCaptor<Cookie> cookieCaptor;

    @Test
    public void nonceCookieTest() {

        mockStatic(IdentityUtil.class);
        Mockito.when(IdentityUtil.getProperty(NONCE_COOKIE_CONFIG)).thenReturn("true");
        assertTrue(SessionNonceCookieUtil.isNonceCookieEnabled());

        // Creating old session nonce and authentication cookie.
        Cookie[] cookies = new Cookie[2];
        cookies[0] = new Cookie(FrameworkConstants.COMMONAUTH_COOKIE, "commonAuthIdValue");
        cookies[1] = new Cookie(NONCE_COOKIE + "-" + OLD_SESSION_DATA_KEY_VALUE, "oldSessionNonceValue");
        Mockito.when(request.getCookies()).thenReturn(cookies);
        Mockito.when(context.getContextIdentifier()).thenReturn(OLD_SESSION_DATA_KEY_VALUE);

        // Validating old session nonce cookie.
        Mockito.when(context.getProperty(cookies[1].getName())).thenReturn(cookies[1].getValue());
        boolean validateNonceCookie = SessionNonceCookieUtil.validateNonceCookie(request, context);
        assertTrue(validateNonceCookie);

        // Stimulate the flow where a new authentication flow is created when there is existing session nonce cookie.
        Mockito.when(context.getContextIdentifier()).thenReturn(NEW_SESSION_DATA_KEY_VALUE);
        SessionNonceCookieUtil.addNonceCookie(request, response, context);

        Mockito.verify(response, times(2)).addCookie(cookieCaptor.capture());
        List<Cookie> capturedCookies = cookieCaptor.getAllValues();

        // First, the old cookie has to be cleared with max age 0.
        Cookie removedOldSessionNonce = capturedCookies.get(0);
        assertEquals(removedOldSessionNonce.getName(), NONCE_COOKIE + "-" + OLD_SESSION_DATA_KEY_VALUE);
        assertEquals(removedOldSessionNonce.getMaxAge(), 0);

        // Then, the new cookie has to be set with max age -1.
        Cookie addedNewSessionNonce = capturedCookies.get(1);
        assertEquals(addedNewSessionNonce.getName(), NONCE_COOKIE + "-" + NEW_SESSION_DATA_KEY_VALUE);
        assertEquals(-1, addedNewSessionNonce.getMaxAge());
    }
}
